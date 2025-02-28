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
  oppoAppKey: string;
  oppoAppSecret: string;
  xiaomiAppId: string;
  xiaomiAppKey: string;
  vivoAppKey: string;
  vivoAppId: string;
  huaweiAppId: string;
  honorAppId: string;
}

const withAliyunConfig: ConfigPlugin<AliPushConfig> = (config, props) => {
  config = withInfoPlist(config, config => {
    Object.assign(config.modResults, {
      Ali_Push_App_Key: props.aliPushAppKey,
      Ali_Push_App_Secret: props.aliPushAppSecret,
      Oppo_App_Key: props.oppoAppKey,
      Oppo_App_Secret: props.oppoAppSecret,
      Xiaomi_App_Id: props.xiaomiAppId,
      Xiaomi_App_Key: props.xiaomiAppKey,
      'com.vivo.push.api_key': props.vivoAppKey,
      'com.vivo.push.app_id': props.vivoAppId,
      'com.huawei.hms.client.appid': props.huaweiAppId,
      'com.hihonor.push.app_id': props.honorAppId,
    });
    return config;
  });

  config = withAndroidManifest(config, config => {
    const mainApplication = AndroidConfig.Manifest.getMainApplicationOrThrow(config.modResults);
    const { manifest } = config.modResults;

    [
      ['Ali_Push_App_Key', props.aliPushAppKey],
      ['Ali_Push_App_Secret', props.aliPushAppSecret],
      ['Oppo_App_Key', props.oppoAppKey],
      ['Oppo_App_Secret', props.oppoAppSecret],
      ['Xiaomi_App_Id', props.xiaomiAppId],
      ['Xiaomi_App_Key', props.xiaomiAppKey],
      ['com.vivo.push.api_key', props.vivoAppKey],
      ['com.vivo.push.app_id', props.vivoAppId],
      ['com.huawei.hms.client.appid', props.huaweiAppId],
      ['com.hihonor.push.app_id', props.honorAppId]
    ].forEach(([key, value]) => {
      AndroidConfig.Manifest.addMetaDataItemToMainApplication(mainApplication, key, value);
    });

    addPermissionsIfMissing(manifest, [
      'POST_NOTIFICATIONS',
      'ACCESS_NETWORK_STATE',
      'DOWNLOAD_WITHOUT_NOTIFICATION'
    ]);

    return config;
  });

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
