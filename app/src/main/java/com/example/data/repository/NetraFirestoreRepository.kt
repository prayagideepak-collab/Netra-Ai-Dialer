package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.model.CallLogRecord
import com.example.model.CrmContactRecord
import com.example.model.IdentityAuditEvent
import com.example.model.VerifiedBusinessIdentity
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NetraFirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: com.google.firebase.auth.FirebaseAuth = Firebase.auth
) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeCallLogs(): Flow<List<CallLogRecord>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/call_logs"
        emitAll(
            db.collection("users").document(uid).collection("call_logs")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CallLogRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.timestampMillis }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createCallLog(record: CallLogRecord): Result<String> {
        return try {
            val uid = requireUserId()
            val docId = record.id.ifBlank { "call_${UUID.randomUUID().toString().replace("-", "").take(16)}" }
            val docRef = db.collection("users").document(uid).collection("call_logs").document(docId)
            val payload = record.copy(
                id = docId,
                userId = uid,
                createdAt = null,
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "unauthenticated"
            handleFirestoreError(e, OperationType.CREATE, "users/$uid/call_logs")
            Result.failure(e)
        }
    }

    suspend fun updateCallLogSummary(
        callId: String,
        existingRecord: CallLogRecord,
        summaryOverview: String,
        discussionPoints: List<String>,
        actionItems: List<String>,
        crmSyncStatus: String,
        crmContactId: String
    ): Result<Unit> {
        return try {
            val uid = requireUserId()
            require(callId.isNotBlank()) { "Call ID must not be blank" }
            requireNotNull(existingRecord.createdAt) { "existingRecord.createdAt must not be null" }
            val docRef = db.collection("users").document(uid).collection("call_logs").document(callId)
            val updated = existingRecord.copy(
                id = callId,
                aiSummaryOverview = summaryOverview.take(2000),
                aiDiscussionPoints = discussionPoints.take(15).map { it.take(300) },
                aiActionItems = actionItems.take(15).map { it.take(300) },
                crmSyncStatus = crmSyncStatus,
                crmContactId = crmContactId,
                updatedAt = null
            )
            docRef.set(updated).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "unauthenticated"
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid/call_logs/$callId")
            Result.failure(e)
        }
    }

    suspend fun getCallLogById(targetUserId: String, callId: String): Result<CallLogRecord?> {
        val path = "users/$targetUserId/call_logs/$callId"
        return try {
            val snapshot = db.collection("users").document(targetUserId)
                .collection("call_logs").document(callId)
                .get()
                .await()
            Result.success(
                snapshot.toObject(
                    CallLogRecord::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    fun observeCrmContacts(): Flow<List<CrmContactRecord>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/crm_contacts"
        emitAll(
            db.collection("users").document(uid).collection("crm_contacts")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CrmContactRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.lastInteractionTimestamp }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createCrmContact(contact: CrmContactRecord): Result<String> {
        return try {
            val uid = requireUserId()
            val docId = contact.id.ifBlank { "crm_${UUID.randomUUID().toString().replace("-", "").take(16)}" }
            val docRef = db.collection("users").document(uid).collection("crm_contacts").document(docId)
            val payload = contact.copy(
                id = docId,
                userId = uid,
                createdAt = null,
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "unauthenticated"
            handleFirestoreError(e, OperationType.CREATE, "users/$uid/crm_contacts")
            Result.failure(e)
        }
    }

    suspend fun updateCrmContact(existing: CrmContactRecord): Result<Unit> {
        return try {
            val uid = requireUserId()
            require(existing.id.isNotBlank()) { "CRM contact ID must not be blank" }
            requireNotNull(existing.createdAt) { "CRM contact createdAt must not be null for update" }
            val docRef = db.collection("users").document(uid).collection("crm_contacts").document(existing.id)
            docRef.set(existing.copy(updatedAt = null)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "unauthenticated"
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid/crm_contacts/${existing.id}")
            Result.failure(e)
        }
    }

    fun observeBusinessIdentity(): Flow<VerifiedBusinessIdentity?> = flow {
        val uid = requireUserId()
        val path = "users/$uid/business_identity"
        emitAll(
            db.collection("users").document(uid).collection("business_identity")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        VerifiedBusinessIdentity::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).firstOrNull()
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveBusinessIdentity(
        identity: VerifiedBusinessIdentity,
        existingIdentity: VerifiedBusinessIdentity?
    ): Result<String> {
        return try {
            val uid = requireUserId()
            val docId = existingIdentity?.id?.ifBlank { "primary_identity" } ?: "primary_identity"
            val docRef = db.collection("users").document(uid).collection("business_identity").document(docId)
            val payload = if (existingIdentity?.createdAt != null) {
                identity.copy(
                    id = docId,
                    userId = uid,
                    createdAt = existingIdentity.createdAt,
                    updatedAt = null
                )
            } else {
                identity.copy(
                    id = docId,
                    userId = uid,
                    createdAt = null,
                    updatedAt = null
                )
            }
            docRef.set(payload).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "unauthenticated"
            handleFirestoreError(e, OperationType.WRITE, "users/$uid/business_identity/primary_identity")
            Result.failure(e)
        }
    }

    fun observeAuditLogs(): Flow<List<IdentityAuditEvent>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/identity_audit_logs"
        emitAll(
            db.collection("users").document(uid).collection("identity_audit_logs")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        IdentityAuditEvent::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun appendAuditEvent(event: IdentityAuditEvent): Result<String> {
        return try {
            val uid = requireUserId()
            val auditId = "audit_${UUID.randomUUID().toString().replace("-", "").take(16)}"
            val docRef = db.collection("users").document(uid).collection("identity_audit_logs").document(auditId)
            val payload = event.copy(
                id = auditId,
                userId = uid,
                createdAt = null
            )
            docRef.set(payload).await()
            Result.success(auditId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "unauthenticated"
            handleFirestoreError(e, OperationType.CREATE, "users/$uid/identity_audit_logs")
            Result.failure(e)
        }
    }
}
