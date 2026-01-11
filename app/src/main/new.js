rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /orders/{orderId} {allow read, write: if true;
    }
    // Optional: Allow other collections too
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}