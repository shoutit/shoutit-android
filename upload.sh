set -e -x
./gradlew --parallel \
--stacktrace \
--project-prop versionSuffix="$CIRCLE_BUILD_NUM" \
crashlyticsUploadDistributionProdRelease crashlyticsUploadDistributionProdStaging
