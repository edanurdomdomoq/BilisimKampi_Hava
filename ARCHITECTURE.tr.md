# HAVA kod yapısı ve bakım rehberi

26 Ekim 2026

Proje, mevcut `com.example.hava` uygulama kimliği korunarak `model / data / ui` katmanlarına ayrılmıştır. Android Studio projesi `BilisimKampi_Hava/` konumundadır.

```text
com.example.hava
├── MainActivity.kt                 Android yaşam döngüsü ve Compose başlangıcı
├── AppContainer.kt                 Uygulama bağımlılıklarını birleştirir
├── model/
│   ├── City.kt                     Şehir kayıtları ve şehir arama yanıtı
│   ├── Forecast.kt                 Saf Current / Hourly / Daily ekran modelleri
│   ├── CityPhoto.kt                Fotoğraf, yazar, kaynak ve lisans
│   └── WeatherCondition.kt         WMO kodlarının Türkçe açıklamaları
├── data/
│   ├── ApiClient.kt                Ortak HTTP istemcileri ve Retrofit servisleri
│   ├── CityApi.kt                  Şehir arama HTTP tanımı
│   ├── ForecastApi.kt              Tahmin HTTP tanımı
│   ├── ForecastResponse.kt         DTO'lar ve sütundan satıra dönüşüm
│   ├── CityRepository.kt           Enjekte edilebilir şehir arama servisi
│   ├── WeatherRepository.kt        API yanıtını Forecast modeline dönüştürür
│   ├── AppPreferences.kt           Dil, ülke ve seçili şehrin kalıcı ayarları
│   ├── WorldCities.kt              Desteklenen ülkeler ve şehir kataloğu
│   ├── TurkiyeSehirleri.kt          Türkiye'nin 81 şehri
│   ├── WikimediaPhotoSource.kt     Wikimedia HTTP ve sağlayıcıya özel kurallar
│   ├── CityPhotoParser.kt          Şehir eşleştirme ve lisans doğrulama
│   ├── CityPhotoCache.kt           Fotoğraf bilgileri için disk önbelleği
│   └── CityPhotoRepository.kt      Toplu yenileme, önbellek ve ortak fotoğraf durumu
└── ui/
    ├── LoadState.kt                Loading / Success / Empty / Error
    ├── HavaDestination.kt          Today / Forecast / Cities hedefleri
    ├── HavaNavHost.kt              Gezinme ve döndürmede saklanan ekran durumu
    ├── cities/
    │   ├── CitiesScreen.kt         Liste, arama ve filtrelerin ekran düzeni
    │   ├── CitiesViewModel.kt      Kartların veri istekleri ve tekrar deneme
    │   ├── CityWeatherCard.kt      Verilen durumu çizen şehir kartı
    │   ├── CountryPicker.kt        Ülke seçim penceresi
    │   ├── CitySearchControls.kt   Arama alanı ve ülke düğmesi
    │   └── PhotoCreditsDialog.kt   Yazar, lisans ve kaynak bağlantıları
    ├── forecast/
    │   ├── ForecastScreen.kt       Ekran durumu ve dikey/yatay yerleşim
    │   ├── ForecastViewModel.kt    Tahmin isteği, tekrar deneme, eski yanıt koruması
    │   └── ForecastPanels.kt       Bugün, saatlik, günlük, yağış ve ölçüm panelleri
    ├── components/
    │   ├── LoadingView.kt          Ortak yükleniyor görünümü
    │   ├── EmptyView.kt            Ortak boş veri görünümü
    │   ├── ErrorView.kt            Ortak hata ve tekrar deneme görünümü
    │   ├── HavaNavigation.kt       Alt gezinme ve yatay gezinme şeridi
    │   ├── CityPhotoImageLoader.kt Ortak Coil önbelleği ve HTTP kimliği
    │   ├── AppLanguage.kt          TR/EN sunum yardımcıları
    │   ├── ResponsiveLayout.kt     Yön bilgisi
    │   ├── WeatherArt.kt           Hava durumu çizimleri
    │   └── LineWeatherIcon.kt      Hava durumu simgeleri
    └── theme/                     Color / Theme / Type
```

## Veri akışı

`Ekran olayı → ViewModel → Repository → Retrofit → DTO → Forecast → LoadState → Ekran`

- Composable şehir kartı doğrudan ağ isteği yapmaz. Kartın durumu ve tekrar deneme olayı dışarıdan verilir.
- ViewModel, ekranın saklanan yaşam süresince aynı şehir için tekrarlanan istekleri birleştirir. Hava istekleri aynı anda en fazla üç tane çalışır.
- Liste, ekrandaki ve önden hazırlanan kartlar için veri ister; tüm arama sonuçlarına birden istek göndermez.
- Tahmin ViewModel'i önceki isteği iptal eder ve eski bir yanıtın yeni şehrin verisini değiştirmesini engeller.
- Saatlik ve günlük API sütunları `toForecast()` içinde kayıtlara dönüştürülür. Eksik zorunlu sütunlara ait satırlar elenir; bilinmeyen yağış olasılığı sıfıra çevrilmez.
- `model/Forecast.kt` ve fotoğraf modelleri Gson ya da Compose'a bağımlı değildir.
- Fotoğraf eşleştirmesi, lisans kontrolü, HTTP erişimi, metadata önbelleği ve Coil görsel önbelleği ayrı dosyalardadır.
- Mevcut dil ve şehir tercihleriyle fotoğraf önbelleği anahtarları korunmuştur.

## Yeni özellik eklerken

1. Bir API alanı değişiyorsa önce `data/ForecastResponse.kt` DTO ve dönüşümünü güncelle.
2. Ekranın kullanacağı anlamlı kaydı `model/` içinde tut. API alan adlarını UI'ya taşıma.
3. Yükleme ve tekrar deneme davranışını ViewModel'e ekle; Composable içinde repository oluşturma.
4. Composable'a veri ve olay callback'leri ver. Ortak yükleme/boş/hata bileşenlerini kullan.
5. Yeni bir ülke/şehir için `WorldCities` kataloğunu; özel fotoğraf eşleştirmesi gerekiyorsa `CityPhotoParser` kurallarını güncelle.
6. İş kuralını birim testiyle, gezinme veya döndürme davranışını cihaz testiyle doğrula.

Kotlin kodu ktfmt 0.59'un Kotlin stiliyle biçimlendirilmiştir. Kök `.editorconfig`, UTF-8, LF ve dört boşluk girinti kurallarını belirtir.

## Çalıştırma ve kontroller

Android Studio'da projeyi aç, `app → Pixel_8 → Run` seç.

Proje klasöründe PowerShell:

```powershell
$env:JAVA_HOME = 'C:/Program Files/Android/Android Studio/jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

Canlı fotoğraf testi internet gerektirir; metadata önbelleğini temizleyerek yeni HTTP yolunu da kontrol eder. Yerel birim testleri gerçek servislere bağlanmaz. Gezinme ve döndürme testleri, `WeatherFixtureRule` ile kontrollü hava durumu yanıtları alır. Böylece servis kesintisi ekran testlerini bozmaz. Gerçek kullanıcı akışında varsayılan repository Open-Meteo kullanır; test verileri yalnızca androidTest APK’sına girer.
