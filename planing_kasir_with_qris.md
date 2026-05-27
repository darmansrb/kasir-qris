Berikut adalah draf rancangan dokumen `.md` untuk aplikasi POS Kasir Offline. Saya telah menyusunnya dengan pendekatan arsitektur modern Android (MVVM) dan menambahkan beberapa koreksi serta fitur esensial untuk standar aplikasi POS, seperti manajemen *state* pesanan, optimalisasi penyimpanan gambar, dan integrasi cetak struk.

---

# 📝 Rencana Pengembangan Aplikasi POS Kasir Offline (QRIS & Tunai)

## 🛠️ Tech Stack & Architecture

* **UI Framework:** Android Jetpack Compose (Material 3)
* **Database:** Room Database (Offline-first architecture)
* **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture principles
* **Asynchronous:** Kotlin Coroutines & Flow (untuk observasi data *real-time* dari Room)
* **Dependency Injection:** Hilt / Dagger
* **Library Pendukung:**
* `Coil`: Memuat dan caching gambar (produk/QRIS).
* `CameraX`: Untuk fitur ambil foto produk dan *scan* QRIS.
* `ZXing` / `Barcode Scanning ML Kit`: Untuk membaca (decode) dan membuat (encode) *generate* QR image.
* `Apache POI` / `OpenCSV` & Android `PdfDocument`: Untuk *export* laporan.



---

## 💾 Struktur Database (Room Entities) - *High Level*

* **`ProductEntity`**: `id`, `name`, `price`, `image_path` (Simpan *path* internal storage, bukan BLOB untuk mencegah DB bengkak), `is_active`.
* **`QrisEntity`**: `id`, `merchant_name`, `raw_qris_string` (Penting untuk generate QRIS dinamis), `is_default`.
* **`OrderEntity`**: `id`, `customer_name_or_table`, `status` (`DRAFT`, `PAID`, `CANCELLED`), `created_at`.
* **`OrderDetailEntity`**: `id`, `order_id`, `product_id`, `product_name_snapshot`, `price_snapshot`, `qty`, `subtotal`. *(Snapshot penting agar laporan tidak berubah jika harga master barang di-update di masa depan).*
* **`TransactionEntity`**: `id`, `order_id`, `total_amount`, `payment_method` (`CASH`, `QRIS`), `payment_date`.

---

## 📱 Alur Fitur & UI/UX

### 1. Halaman Menu Pemesanan (Order Management) - STRICT PHONE VERSION (Bottom Navigation Only)

**Fokus Utama:** UX yang ergonomis, cepat, dan terfokus untuk kasir di perangkat Handphone dengan navigasi bar bawah.

* **Daftar Pesanan Aktif (Halaman Utama Order)**
    * Menampilkan daftar pesanan berjalan (`status = DRAFT`) secara eksklusif.
    * Bilah pencarian (search filter) di bagian atas untuk menyaring pesanan aktif berdasarkan nama pelanggan atau nomor kursi.
    * *Floating Action Button* (FAB) berupa tombol bulat `+` di pojok kanan bawah untuk menambahkan pesanan baru, yang akan membuka **Halaman Baru** (Katalog & Keranjang Pemesanan).
    * Klik pada item pesanan: Membuka **Halaman Baru** untuk edit pesanan atau langsung melakukan checkout pembayaran.
    * Aksi hapus pesanan langsung di baris daftar.

* **Halaman Baru: Tambah/Edit Pesanan (Katalog Produk & Cart)**
    * Memisahkan workspace katalog dari menu list pesanan untuk menghemat ruang layar Handphone.
    * Input Nama Pelanggan / Nomor Kursi.
    * Pencarian produk secara reaktif.
    * *Grid Card* Produk: Foto, Nama, Harga, dan tombol cepat `+` / `-` untuk langsung menambah ke keranjang belanja.
    * *Bottom Sheet* / *Sticky Bottom Bar* Keranjang: Menampilkan ringkasan total item & total harga secara real-time.
    * Tombol **"Simpan Pesanan"** (menyimpan sebagai `DRAFT` dan kembali ke halaman utama list pesanan) atau **"Bayar Sekarang"** (langsung ke kasir pembayaran).




### 2. Halaman Kalkulator & Pembayaran (Dynamic QRIS)

**Fokus Utama:** Fleksibilitas pembayaran tanpa harus masuk ke menu order (untuk pesanan custom) atau melunasi pesanan yang ada.

* **Kalkulator Standar:** Fungsi matematika dasar (+, -, *, /) dengan *display* angka besar.
* **Tombol "Generate QRIS Dinamis":**
* *Logic Engine:* Mengambil *String* QRIS statis dari database. Sistem akan mem- *parsing* format EMVCo, menghapus Tag nilai lama (jika ada), dan meng- *inject* Tag 54 (Transaction Amount) dengan hasil kalkulasi, lalu me- *recalculate* CRC checksum di akhir string.
* Menampilkan *barcode* QRIS dinamis di layar untuk di-*scan* pembeli.


* **Tombol "Bayar Tunai":** Menampilkan kalkulasi kembalian otomatis berdasarkan input uang yang diterima.

### 3. Halaman Menu Pengaturan (Master Data & Config)

**Fokus Utama:** Manajemen data mandiri (*offline*).

* **Manajemen Produk (Input Barang)**
* Form Input: Nama, Harga, Upload Foto (Pilihan: Kamera via CameraX atau Galeri via `ActivityResultContracts.GetContent()`).
* *Koreksi Sistem:* Gambar yang dipilih/difoto harus di- *compress* dan disalin ke `Context.filesDir` (Internal Storage), kemudian *path*-nya saja yang disimpan ke Room.
* Daftar produk dengan opsi *Long press* (Edit/Hapus). *Hapus* sebaiknya menggunakan *Soft Delete* (`is_active = false`) agar riwayat transaksi masa lalu tidak *error* (Missing reference).


* **Manajemen QRIS**
* Input QRIS: Bisa via Galeri, Scan Kamera langsung, atau Paste *Text Raw QRIS*.
* *Auto-detect:* Mengekstrak Tag 59 (Merchant Name) dari *string* QRIS secara otomatis untuk mengisi kolom nama.


* **Preferensi Aplikasi**
* Tema (Light/Dark/System).
* Pengaturan Printer (Fitur Tambahan - lihat di bawah).
* Database *Backup/Restore* (Export `.db` file ke penyimpanan lokal, sangat krusial untuk aplikasi kasir *offline*).



### 4. Halaman Laporan Riwayat Transaksi (Reporting)

**Fokus Utama:** Visibilitas arus kas dan audit.

* **Filter & Dashboard**
* Filter: *Date Range Picker* (Rentang Tanggal) & Jam.
* *Summary Cards*: Total Pendapatan (Rp), Total Transaksi, Rasio Tunai vs QRIS (dalam *Pie Chart* sederhana).


* **Daftar Transaksi**
* *List* transaksi yang diurutkan dari yang terbaru (`ORDER BY payment_date DESC`).
* Mendukung *Pagination* (menggunakan Paging 3) jika data transaksi sudah ribuan agar UI tidak *lag*.
* *Click Action*: Membuka *Detail Screen* (Nama Kasir/Nomor Meja, Rincian Item, Harga Snapshot, Metode Bayar).


* **Export Laporan**
* Tombol *Export* ke CSV (menggunakan `OpenCSV`) dan PDF (menggunakan `PdfDocument` bawaan Android). Disimpan ke `Downloads` folder agar mudah dibagikan via WhatsApp/Email.



---

## 💡 Fitur Tambahan & Koreksi (Best Practices Kasir Offline)

1. **Struktur *Price Snapshot* pada Relasi Database (Koreksi):**
Seperti yang disebutkan di skema database, Anda tidak boleh melakukan `JOIN` langsung ke `ProductEntity` untuk mendapatkan harga di menu "Laporan". Harga harus di-*copy* (snapshot) ke `OrderDetailEntity` saat transaksi terjadi. Jika harga Indomie naik bulan depan, laporan bulan ini tidak boleh ikut berubah.
2. **Modul Cetak Struk / *Thermal Bluetooth Printer*:**
Aplikasi POS belum lengkap tanpa cetak struk fisik. Anda bisa menambahkan utilitas ESC/POS (misalnya library `Dantsu` atau modul *socket* Bluetooth bawaan) untuk menge- *print* *bill* pesanan dan struk lunas.
3. **Kalkulasi Pajak & Diskon (Opsional):**
Menambahkan opsi PPN/PB1 (misal 10% atau 11%) atau kolom diskon nominal/persen di halaman "Pembayaran".
4. **Alur Validasi QRIS:**
Karena ini aplikasi offline, verifikasi pembayaran QRIS tidak bisa otomatis memanggil API bank. Anda perlu menambahkan tombol konfirmasi manual **"Sudah Dibayar"** bagi kasir setelah melihat mutasi di *mobile banking* mereka atau mesin EDC.

---