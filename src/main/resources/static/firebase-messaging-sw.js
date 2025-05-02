// firebase-messaging-sw.js

// Firebase App 및 Messaging 라이브러리(구 버전 호환) 불러오기
importScripts("https://www.gstatic.com/firebasejs/9.22.2/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/9.22.2/firebase-messaging-compat.js");

// Firebase 초기화 (index.html과 동일한 구성)
firebase.initializeApp({
    apiKey: "AIzaSyCzEr1maXbPtYRHxwDnD75yDILtwHxHT0U",
    authDomain: "parking-10easy.firebaseapp.com",
    projectId: "parking-10easy",
    storageBucket: "parking-10easy.firebasestorage.app",
    messagingSenderId: "834731642110",
    appId: "1:834731642110:web:04c07f7c8bdb7e3fbc69af",
    measurementId: "G-B1YY4TW0E8"
});

const messaging = firebase.messaging();

// 백그라운드 메시지 수신 시 처리 로직
messaging.onBackgroundMessage(function(payload) {
    console.log('[firebase-messaging-sw.js] 백그라운드 메시지 수신:', payload);
    // 기본 알림 옵션 구성 예제
    const notificationTitle = payload.notification.title;
    const notificationOptions = {
        body: payload.notification.body,
        // icon: '/firebase-logo.png'  // 아이콘 파일 경로
    };

    self.registration.showNotification(notificationTitle, notificationOptions);
});
