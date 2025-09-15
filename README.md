Ini adalah aplikasi untuk memendekkan sebuah URL ke URL lain yang lebih sederhana dan mudah diingat.

Teknologi yang digunakan dalam pengembangan aplikasi ini mencakup:
- Java 21
- Springboot 3.5.5
- PostgreSQL
- Redis
- Intellij Idea IDE
- pgAdmin 4

Justifikasi
- Dipilihnya java 21 karena sifatnya yang LTS (Long Term Service), di mana masih disupport sampai 2028. Java dengan versi lebih baru tidak dipilih karena hanya disupport 6 bulan.
- Springboot 3.5.5 dipilih dengan alasan yang sama
- PosgreSQL (mewakili SQL secara umum) dipilih dibandingkan dengan NoSQL karena:
  - Skema , setiap tabel memiliki kolom dan tipe data yang jelas
  - Integritas dan konsistensi tinggi
  - Bahasa SQL relatif lebih mudah dipelajari
  - Mendukung relasi antar tabel
  - Fitur rollback, recovery dan backup sangat matang
- Redis dipilih karena menawarkan kecepatan tinggi, struktur data fleksibel dan kemampuan caching

Langkah-langkah menjalankan aplikasi:
1. Pastikan sudah terinstall java versi 21
2. Pastikan sudah terinstall dan running PostgreSQL 17
3. Pastikan sudah terinstall dan running Redis (jika kamu menggunakan sistem operasi windows, sila merujuk pada https://redis.io/docs/latest/operate/oss_and_stack/install/archive/install-redis/install-redis-on-windows/ untuk instruksi penginstallan)
4. Clone git repository ini
5. Jalankan mvn clean install spring-boot:run untuk menjalankan aplikasi
6. Coba di swagger atau postman
