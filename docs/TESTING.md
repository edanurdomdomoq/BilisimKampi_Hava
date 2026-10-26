# Test rehberi

## Yerel kontroller

Proje kökünde:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

## Cihaz kontrolleri

API 24 veya üzeri bir emülatör ya da fiziksel cihaz bağla. Son doğrulama API 37 üzerinde yapılmıştır.

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Canlı API testleri internet bağlantısı gerektirir. `CityPhotoApiTest`, yeni HTTP yolunu denemek için fotoğraf metadata önbelleğini temizler. Dil ve şehir tercihleri korunur.

## Test kapsamı

| Sınıf | Adet | Kapsam |
|---|---:|---|
| CityDataTest | 5 | Şehir katalogları, plakalar, koordinatlar ve arama |
| CityPhotoTest | 6 | Şehir eşleştirme, yönlendirme, koordinat ve lisans doğrulama |
| ForecastMapperTest | 6 | JSON alanları, eksik sütunlar, saatlik/günlük dönüşüm ve sınır değerleri |
| ExampleUnitTest | 1 | Mevcut temel kontrol |
| CityPhotoApiTest | 1 | 109 şehir için canlı fotoğraf ve lisans; görsel indirme/çözme |
| WeatherApiTest | 1 | Canlı tahmin yanıtının güncel/saatlik/günlük modellere dönüşümü |
| ForecastRecoveryTest | 2 | Hata sonrası tekrar deneme ve aynı koordinatların tekrar yüklenmemesi |
| LandscapeTest | 2 | Döndürme, arama, dil, ülke penceresi ve seçilen şehir |
| ViewModelRequestTest | 3 | Eski yanıt koruması, istek birleştirme, eşzamanlılık ve tekrar deneme |
| WeatherNavigationTest | 1 | Ülke, dil, arama, sayfalama ve tahmin gezinmesi |
| ExampleInstrumentedTest | 1 | Uygulama paketi kontrolü |

Ekran testleri kontrollü `WeatherFixtureRule` verisi kullanır; üretim uygulaması gerçek `WeatherRepository` kullanır. Test verileri yalnızca test APK'sına dahil edilir.

## Son doğrulama

26 Ekim 2026: debug derlemesi başarılı, 18 birim testi ve 11 cihaz testi geçti. Canlı fotoğraf testi 109/109 şehir eşleştirdi. Lint sonucu: 0 hata, 68 uyarı.

Bu sonuçlar yerel doğrulamaya aittir; GitHub Actions sonucu değildir. Canlı servislerin erişilebilirliği test çalıştırma anına bağlıdır.

## Raporlar

- Birim test raporu: `app/build/reports/tests/testDebugUnitTest/index.html`
- Lint: `app/build/reports/lint-results-debug.html`
- Cihaz test raporu: `app/build/reports/androidTests/connected/`
