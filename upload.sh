set -e -x
if [ ${CI_BUILD_REF_NAME} == "master" ] || [ ${CI_BUILD_REF_NAME} == "develop" ];
then
	./gradlew --parallel \
	--stacktrace \
	--project-prop commitId="${CI_BUILD_REF}" \
	--project-prop versionSuffix="$CI_BUILD_REF_NAME.$CI_BUILD_ID" \
	crashlyticsUploadDistributionProdRelease
fi
