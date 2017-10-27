package com.shoutit.app.android.view.invitefriends.contactsfriends;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.api.model.Contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
    public List<Contact> getAllPhoneContactsSorted() {
        final List<Contact> allPhoneContacts = new ArrayList<>(getAllPhoneContacts());

        Collections.sort(allPhoneContacts, (contact1, contact2) ->
                Strings.nullToEmpty(contact1.getName())
                        .compareToIgnoreCase(Strings.nullToEmpty(contact2.getName())));

        return ImmutableList.copyOf(allPhoneContacts);
    }

    @Nonnull
    public List<Contact> getAllPhoneContacts() {
        final Map<String, Contact> contactsMap = new LinkedHashMap<>();

        getPhoneNumbers(contactsMap);
        getEmails(contactsMap);

        return ImmutableList.copyOf(contactsMap.values());
    }

    private void getPhoneNumbers(@Nonnull Map<String, Contact> contactsMap) {
        final Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.Data.LOOKUP_KEY,
                        DISPLAY_NAME_COLUMN,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
                },
                DISPLAY_NAME_COLUMN + " IS NOT NULL",
                null,
                null);

        if (phoneCursor != null) {
            try {
                for (phoneCursor.moveToFirst(); !phoneCursor.isAfterLast(); phoneCursor.moveToNext()) {
                    final String lookUpKey = phoneCursor.getString(0);
                    final String name = phoneCursor.getString(1);
                    final String phone = phoneCursor.getString(2);
                    final String normalizedPhone = phoneCursor.getString(3);

                    final String phoneNumber = TextUtils.isEmpty(normalizedPhone) ?
                            phone : normalizedPhone;

                    if (contactsMap.containsKey(lookUpKey)) {
                        final Contact contact = contactsMap.get(lookUpKey);
                        contactsMap.put(lookUpKey, contact.withNewMobile(name, phoneNumber));
                    } else {
                        contactsMap.put(lookUpKey, new Contact(name, Lists.newArrayList(phoneNumber), Lists.newArrayList()));
                    }
                }
            } finally {
                phoneCursor.close();
            }
        }
    }

    private void getEmails(@Nonnull Map<String, Contact> contactsMap) {
        final Cursor emailCursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[]{ContactsContract.Data.LOOKUP_KEY,
                        DISPLAY_NAME_COLUMN,
                        ContactsContract.CommonDataKinds.Email.ADDRESS
                },
                DISPLAY_NAME_COLUMN + " IS NOT NULL",
                null,
                null);

        if (emailCursor != null) {
            try {
                for (emailCursor.moveToFirst(); !emailCursor.isAfterLast(); emailCursor.moveToNext()) {
                    final String lookUpKey = emailCursor.getString(0);
                    final String name = emailCursor.getString(1);
                    final String email = emailCursor.getString(2);

                    if (contactsMap.containsKey(lookUpKey)) {
                        final Contact contact = contactsMap.get(lookUpKey);
                        contactsMap.put(lookUpKey, contact.withNewEmail(name, email));
                    } else {
                        contactsMap.put(lookUpKey, new Contact(name, Lists.newArrayList(), Lists.newArrayList(email)));
                    }
                }
            } finally {
                emailCursor.close();
            }
        }
    }
}