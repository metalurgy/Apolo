package com.bitacora.pro.contacts

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

object ContactImportHelper {
    private const val TAG = "ContactImport"

    /**
     * Launch the phone-specific contact picker.
     * This is the most reliable way to get both name and phone number.
     */
    @Composable
    fun rememberContactPickerLauncher(
        onContactSelected: (name: String, phone: String) -> Unit,
        onError: (message: String) -> Unit
    ) = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Log.d(TAG, "Selected URI: $uri")
                parsePhoneContact(uri, onContactSelected, onError)
            } ?: run {
                Log.w(TAG, "No URI returned from contact picker")
                onError("No se seleccionó contacto")
            }
        } else {
            Log.d(TAG, "Contact picker cancelled")
        }
    }

    /**
     * Parse the selected contact URI to extract name and phone.
     * This handles the phone-specific picker result.
     */
    private fun parsePhoneContact(
        uri: android.net.Uri,
        onContactSelected: (name: String, phone: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        try {
            val context = android.app.Application()
            val contentResolver = context.contentResolver

            // Query the phone-specific URI directly
            val cursor = contentResolver.query(
                uri,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
                ),
                null,
                null,
                null
            )

            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val displayNameIndex = c.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    )
                    val numberIndex = c.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )
                    val normalizedIndex = c.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
                    )

                    val displayName = c.getString(displayNameIndex) ?: ""
                    val rawNumber = c.getString(numberIndex) ?: ""
                    val normalizedNumber = c.getString(normalizedIndex) ?: ""

                    Log.d(TAG, "Display name: $displayName")
                    Log.d(TAG, "Raw number: $rawNumber")
                    Log.d(TAG, "Normalized number: $normalizedNumber")

                    // Use normalized number if available, otherwise use raw number
                    val finalPhone = normalizedNumber.takeIf { it.isNotBlank() } ?: rawNumber

                    Log.d(TAG, "Final phone: $finalPhone")

                    if (displayName.isBlank()) {
                        onError("No se pudo leer el nombre del contacto")
                    } else if (finalPhone.isBlank()) {
                        onError("No se pudo leer el teléfono del contacto. Puedes escribirlo manualmente.")
                    } else {
                        onContactSelected(displayName, finalPhone)
                    }
                } else {
                    Log.w(TAG, "Cursor is empty")
                    onError("No se pudo leer el contacto")
                }
            } ?: run {
                Log.w(TAG, "Cursor is null")
                onError("No se pudo acceder al contacto")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing contact", e)
            onError("Error al leer el contacto: ${e.message}")
        }
    }

    /**
     * Create the intent to launch the phone-specific contact picker.
     * This is more reliable than the generic contact picker.
     */
    fun createPhonePickerIntent(): Intent {
        return Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
    }

    /**
     * Fallback: Query contact by ID if the direct picker doesn't return a phone row.
     * This handles edge cases where the picker returns a contact ID instead.
     */
    fun queryPhonesByContactId(
        context: Context,
        contactId: String,
        onPhonesFound: (phones: List<Pair<String, String>>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
                ),
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(contactId),
                null
            )

            cursor?.use { c ->
                val phones = mutableListOf<Pair<String, String>>()
                while (c.moveToNext()) {
                    val numberIndex = c.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )
                    val normalizedIndex = c.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
                    )

                    val rawNumber = c.getString(numberIndex) ?: ""
                    val normalizedNumber = c.getString(normalizedIndex) ?: ""
                    val finalPhone = normalizedNumber.takeIf { it.isNotBlank() } ?: rawNumber

                    if (finalPhone.isNotBlank()) {
                        phones.add(Pair(finalPhone, finalPhone))
                    }
                }

                Log.d(TAG, "Found ${phones.size} phone(s) for contact $contactId")

                if (phones.isEmpty()) {
                    onError("Este contacto no tiene teléfono registrado")
                } else {
                    onPhonesFound(phones)
                }
            } ?: run {
                onError("No se pudo acceder al contacto")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying phones by contact ID", e)
            onError("Error al leer teléfono: ${e.message}")
        }
    }
}
