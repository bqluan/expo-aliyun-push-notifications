import {
  withInfoPlist,
  withAndroidManifest,
  AndroidConfig,
  ConfigPlugin,
  withProjectBuildGradle
} from 'expo/config-plugins';

interface AliPushConfig {
  aliPushAppKey: string;
  aliPushAppSecret: string;
}
const withAliyunConfig: ConfigPlugin<AliPushConfig> = (config, { aliPushAppKey, aliPushAppSecret }) => {
  config = withInfoPlist(config, config => {
    config.modResults['Ali_Push_App_Key'] = aliPushAppKey;
    config.modResults['Ali_Push_App_Secret'] = aliPushAppSecret;
    return config;
  });

  config = withAndroidManifest(config, config => {
    const mainApplication = AndroidConfig.Manifest.getMainApplicationOrThrow(config.modResults);
    const { manifest } = config.modResults;

    AndroidConfig.Manifest.addMetaDataItemToMainApplication(
      mainApplication,
      'Ali_Push_App_Key',
      aliPushAppKey
    );

    AndroidConfig.Manifest.addMetaDataItemToMainApplication(
      mainApplication,
      'Ali_Push_App_Secret',
      aliPushAppSecret
    );
    // 需要添加的权限列表
    const requiredPermissions = [
      'POST_NOTIFICATIONS',
      'ACCESS_NETWORK_STATE',
      'DOWNLOAD_WITHOUT_NOTIFICATION',
    ];
    addPermissionsIfMissing(manifest, requiredPermissions);

    return config;
  });
  // 修改 android/build.gradle，添加阿里云和华为的 maven 仓库地址
  config = withProjectBuildGradle(config, config => {
    if (!config.modResults.contents.includes("https://maven.aliyun.com/nexus/content/repositories/releases/")) {
      config.modResults.contents = config.modResults.contents.replace(
        /allprojects\s*{\s*repositories\s*{/,
        `allprojects {\n    repositories {\n        maven {\n            url 'https://maven.aliyun.com/nexus/content/repositories/releases/'\n        }\n        maven {\n            url 'https://developer.huawei.com/repo/'\n        }`
      );
    }
    return config;
  });

  return config;
};

function addPermissionsIfMissing(manifest: any, permissionNames: string[]): void {
  if (!manifest['uses-permission']) {
    manifest['uses-permission'] = [];
  }

  const permissions = manifest['uses-permission'];

  permissionNames.forEach(permissionName => {
    const fullPermissionName = `android.permission.${permissionName}`;

    const hasPermission = permissions.some(
      (perm: any) => perm.$['android:name'] === fullPermissionName
    );

    if (!hasPermission) {
      permissions.push({
        $: { 'android:name': fullPermissionName },
      });
    }
  });
}

export default withAliyunConfig;
