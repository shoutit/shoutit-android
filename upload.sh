set -e -x
./gradlew --parallel \
--stacktrace \
--project-prop versionSuffix="$HASH" \
crashlyticsUploadDistributionProdRelease crashlyticsUploadDistributionProdStaging
