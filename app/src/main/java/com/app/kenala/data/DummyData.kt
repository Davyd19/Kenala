package com.app.kenala.data

// 1. Definisikan struktur data Jurnal di satu tempat.
data class Journal(
    val id: Int,
    val title: String,
    val date: String,
    val story: String,
    val imageUrl: String
)

// 2. Buat daftar dummy sebagai satu-satunya "sumber kebenaran".
val journalList = listOf(
    Journal(
        id = 1,
        title = "Secangkir Ketenangan di Kopi Seroja",
        date = "15 Okt 2025",
        story = "Menemukan kedai kopi tersembunyi ini adalah sebuah anugerah. Suasananya tenang, kopinya nikmat, dan aku bisa membaca buku selama berjam-jam tanpa gangguan. Dinding bata ekspos dan aroma biji kopi yang baru digiling menciptakan suasana yang sangat nyaman. Ini adalah tempat yang akan sering aku kunjungi untuk melarikan diri dari hiruk pikuk kota.",
        imageUrl = "https://images.pexels.com/photos/312418/pexels-photo-312418.jpeg"
    ),
    Journal(
        id = 2,
        title = "Pameran Seni Kontemporer 'Ruang Hening'",
        date = "11 Okt 2025",
        story = "Tidak menyangka ada galeri seni sekecil ini di tengah kota. Lukisan-lukisannya sangat menyentuh dan membuatku berpikir tentang banyak hal. Setiap karya seolah bercerita tentang keresahan dan harapan. Aku menghabiskan waktu hampir dua jam di sini, merenungi setiap detail. Pengalaman yang sangat introspektif.",
        imageUrl = "https://images.pexels.com/photos/1025804/pexels-photo-1025804.jpeg"
    ),
    Journal(
        id = 3,
        title = "Menyatu dengan Alam di Hutan Kota",
        date = "05 Okt 2025",
        story = "Udara segar dan suara alam di sini benar-benar menyegarkan. Tempat yang sempurna untuk lari pagi atau sekadar berjalan-jalan santai di akhir pekan. Aku mengikuti jalur setapak hingga ke puncak bukit kecil dan pemandangan kota dari atas sana sungguh luar biasa. Rasanya semua beban pikiran hilang seketika.",
        imageUrl = "https://images.pexels.com/photos/167699/pexels-photo-167699.jpeg"
    )
)
