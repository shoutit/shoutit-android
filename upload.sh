set -e -x
./gradlew --parallel \
--stacktrace \
crashlyticsUploadDistributionProdRelease crashlyticsUploadDistributionProdStaging
