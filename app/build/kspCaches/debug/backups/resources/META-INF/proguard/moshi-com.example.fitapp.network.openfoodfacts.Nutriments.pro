-if class com.example.fitapp.network.openfoodfacts.Nutriments
-keepnames class com.example.fitapp.network.openfoodfacts.Nutriments
-if class com.example.fitapp.network.openfoodfacts.Nutriments
-keep class com.example.fitapp.network.openfoodfacts.NutrimentsJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
