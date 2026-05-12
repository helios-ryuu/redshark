package com.helios.redshark.data.remote.r2

import com.helios.redshark.BuildConfig
import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class R2Client @Inject constructor(
    private val okHttpClient: OkHttpClient,
) {
    private val endpoint = BuildConfig.CLOUDFLARE_R2_ENDPOINT
    private val bucket = BuildConfig.CLOUDFLARE_R2_BUCKET
    private val accessKeyId = BuildConfig.CLOUDFLARE_R2_ACCESS_KEY_ID
    private val secretAccessKey = BuildConfig.CLOUDFLARE_R2_SECRET_ACCESS_KEY
    private val publicBaseUrl = BuildConfig.CLOUDFLARE_R2_PUBLIC_BASE_URL

    suspend fun putObject(key: String, bytes: ByteArray, contentType: String): Result<String> {
        return withContext(Dispatchers.IO) { try {
            val now = Date()
            val dateStamp = dateStampFormat.format(now)
            val amzDate = amzDateFormat.format(now)

            val host = endpoint.removePrefix("https://").removePrefix("http://")
            val url = "$endpoint/$bucket/$key"
            val payloadHash = sha256Hex(bytes)

            val canonicalHeaders = buildString {
                append("content-type:$contentType\n")
                append("host:$host\n")
                append("x-amz-content-sha256:$payloadHash\n")
                append("x-amz-date:$amzDate\n")
            }
            val signedHeaders = "content-type;host;x-amz-content-sha256;x-amz-date"

            val canonicalRequest = buildString {
                append("PUT\n")
                append("/$bucket/$key\n")
                append("\n")
                append(canonicalHeaders)
                append("\n")
                append(signedHeaders)
                append("\n")
                append(payloadHash)
            }

            val credentialScope = "$dateStamp/auto/s3/aws4_request"
            val stringToSign = buildString {
                append("AWS4-HMAC-SHA256\n")
                append("$amzDate\n")
                append("$credentialScope\n")
                append(sha256Hex(canonicalRequest.toByteArray()))
            }

            val signingKey = deriveSigningKey(secretAccessKey, dateStamp, "auto", "s3")
            val signature = hmacSha256Hex(signingKey, stringToSign)

            val authHeader = "AWS4-HMAC-SHA256 " +
                "Credential=$accessKeyId/$credentialScope, " +
                "SignedHeaders=$signedHeaders, " +
                "Signature=$signature"

            val requestBody = bytes.toRequestBody(contentType.toMediaType())
            val request = Request.Builder()
                .url(url)
                .put(requestBody)
                .header("Content-Type", contentType)
                .header("Host", host)
                .header("x-amz-content-sha256", payloadHash)
                .header("x-amz-date", amzDate)
                .header("Authorization", authHeader)
                .build()

            val response = okHttpClient.newCall(request).execute()
            response.use {
                if (it.isSuccessful) {
                    val publicUrl = "$publicBaseUrl/$key"
                    Timber.d("R2 upload success: $publicUrl")
                    Result.Success(publicUrl)
                } else {
                    val body = it.body.string()
                    Timber.e("R2 upload failed: ${it.code} $body")
                    Result.Error(
                        AppException.StorageException("R2 upload failed (${it.code}): $body")
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "R2 putObject exception")
            Result.Error(AppException.StorageException(e.message ?: "R2 upload failed", e))
        } }
    }

    private fun deriveSigningKey(
        secret: String,
        dateStamp: String,
        region: String,
        service: String,
    ): ByteArray {
        val kDate = hmacSha256("AWS4$secret".toByteArray(), dateStamp)
        val kRegion = hmacSha256(kDate, region)
        val kService = hmacSha256(kRegion, service)
        return hmacSha256(kService, "aws4_request")
    }

    private fun hmacSha256(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray())
    }

    private fun hmacSha256Hex(key: ByteArray, data: String): String =
        hmacSha256(key, data).toHex()

    private fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(bytes).toHex()
    }

    private fun sha256Hex(input: String): String = sha256Hex(input.toByteArray())

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    companion object {
        private val amzDateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        private val dateStampFormat = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
}
