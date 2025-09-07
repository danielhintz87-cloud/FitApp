-if class com.example.fitapp.network.openfoodfacts.SearchResponse
-keepnames class com.example.fitapp.network.openfoodfacts.SearchResponse
-if class com.example.fitapp.network.openfoodfacts.SearchResponse
-keep class com.example.fitapp.network.openfoodfacts.SearchResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
