package com.shoutit.app.android.view.invitefriends.contactsfriends;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.api.model.Contact;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class PhoneContactsHelper {

    @Nonnull
    private final ContentResolver contentResolver;

    @Inject
    public PhoneContactsHelper(@Nonnull ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    @SuppressLint("InlinedApi")
    public static final String DISPLAY_NAME_COLUMN = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
            ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
            ContactsContract.Contacts.DISPLAY_NAME;

    @Nonnull
    public List<Contact> getAllPhoneContacts() {
        final Map<String, Contact> contactsMap = new HashMap<>();

        getPhoneNumbers(contactsMap);
        getEmails(contactsMap);

        return ImmutableList.copyOf(contactsMap.values());
    }

    private void getPhoneNumbers(@Nonnull Map<String, Contact> contactsMap) {
        final Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID,
                        DISPLAY_NAME_COLUMN,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                DISPLAY_NAME_COLUMN + " IS NOT NULL",
                null,
                DISPLAY_NAME_COLUMN + " COLLATE LOCALIZED ASC");

        if (phoneCursor != null) {
            try {
                for (phoneCursor.moveToFirst(); !phoneCursor.isAfterLast(); phoneCursor.moveToNext()) {
                    final String userId = phoneCursor.getString(0);
                    final String name = phoneCursor.getString(1);
                    final String phone = phoneCursor.getString(2);

                    if (contactsMap.containsKey(userId)) {
                        final Contact contact = contactsMap.get(userId);
                        contactsMap.put(userId, contact.withNewMobile(name, phone));
                    } else {
                        contactsMap.put(userId, new Contact(name, Lists.newArrayList(phone), Lists.newArrayList()));
                    }
                }
            } finally {
                phoneCursor.close();
            }
        }
    }

    private void getEmails(@Nonnull Map<String, Contact> contactsMap) {
        final Cursor emailCursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID,
                        DISPLAY_NAME_COLUMN,
                        ContactsContract.CommonDataKinds.Email.ADDRESS
                },
                DISPLAY_NAME_COLUMN + " IS NOT NULL",
                null,
                DISPLAY_NAME_COLUMN + " COLLATE LOCALIZED ASC");

        if (emailCursor != null) {
            try {
                for (emailCursor.moveToFirst(); !emailCursor.isAfterLast(); emailCursor.moveToNext()) {
                    final String userId = emailCursor.getString(0);
                    final String name = emailCursor.getString(1);
                    final String email = emailCursor.getString(2);

                    if (contactsMap.containsKey(userId)) {
                        final Contact contact = contactsMap.get(userId);
                        contactsMap.put(userId, contact.withNewEmail(name, email));
                    } else {
                        contactsMap.put(userId, new Contact(name, Lists.newArrayList(), Lists.newArrayList(email)));
                    }
                }
            } finally {
                emailCursor.close();
            }
        }
    }
}