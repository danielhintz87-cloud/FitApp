-if class com.example.fitapp.network.openfoodfacts.ProductResponse
-keepnames class com.example.fitapp.network.openfoodfacts.ProductResponse
-if class com.example.fitapp.network.openfoodfacts.ProductResponse
-keep class com.example.fitapp.network.openfoodfacts.ProductResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
