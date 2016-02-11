set -e -x
./gradlew --parallel \
--stacktrace \
--project-prop versionSuffix="$BRANCH_NAME" \
crashlyticsUploadDistributionProdRelease crashlyticsUploadDistributionProdStaging
