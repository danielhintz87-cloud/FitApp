-if class com.example.fitapp.network.openfoodfacts.Product
-keepnames class com.example.fitapp.network.openfoodfacts.Product
-if class com.example.fitapp.network.openfoodfacts.Product
-keep class com.example.fitapp.network.openfoodfacts.ProductJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
