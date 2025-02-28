import { withInfoPlist, withAndroidManifest, AndroidConfig, ConfigPlugin, withProjectBuildGradle, withDangerousMod } from 'expo/config-plugins';
import fs from 'fs';
import path from 'path';

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
  // 修改 iOS info.plist
  config = modifyInfoPlist(config, props);
  // 修改 AndroidManifest.xml
  config = modifyAndroidManifest(config, props);
  // 修改 android project.build.gradle
  config = modifyProjectBuildGradle(config);
  // 修改 android proguard-rules.pro
  config = modifyProguardRules(config);
  return config;
};

export default withAliyunConfig;

function modifyInfoPlist(config: any, props: AliPushConfig) {
  return withInfoPlist(config, config => {
    Object.assign(config.modResults, {
      Ali_Push_App_Key: props.aliPushAppKey,
      Ali_Push_App_Secret: props.aliPushAppSecret
    });
    return config;
  });
}

function modifyAndroidManifest(config: any, props: AliPushConfig) {
  return withAndroidManifest(config, config => {
    const mainApplication = AndroidConfig.Manifest.getMainApplicationOrThrow(config.modResults);
    const {manifest} = config.modResults;

    addMetaDataItems(mainApplication, props);
    addPermissions(manifest, [
      'POST_NOTIFICATIONS',
      'ACCESS_NETWORK_STATE',
      'DOWNLOAD_WITHOUT_NOTIFICATION'
    ]);

    return config;
  });
}

function addMetaDataItems(mainApplication: any, props: AliPushConfig) {
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
}

function modifyProjectBuildGradle(config: any) {
  return withProjectBuildGradle(config, config => {
    if (!config.modResults.contents.includes("https://maven.aliyun.com/nexus/content/repositories/releases/")) {
      config.modResults.contents = config.modResults.contents.replace(
        /allprojects\s*{\s*repositories\s*{/,
        `allprojects {\n    repositories {\n        maven {\n            url 'https://maven.aliyun.com/nexus/content/repositories/releases/'\n        }\n        maven {\n            url 'https://developer.huawei.com/repo/'\n        }`
      );
    }
    return config;
  });
}

function modifyProguardRules(config: any) {
  return withDangerousMod(config, [
    'android',
    (config) => {
      const proguardPath = path.join(config.modRequest.platformProjectRoot, 'app/proguard-rules.pro');

      if (fs.existsSync(proguardPath)) {
        let proguardContent = fs.readFileSync(proguardPath, 'utf-8');

        if (!proguardContent.includes('# 阿里云移动推送')) {
          proguardContent += getProguardRules();
          fs.writeFileSync(proguardPath, proguardContent, 'utf-8');
        }
      }
      return config;
    },
  ]);
}

function getProguardRules(): string {
  return `
# 阿里云移动推送
-keepclasseswithmembernames class ** { native <methods>; }
-keepattributes Signature
-keep class sun.misc.Unsafe { *; }
-keep class com.taobao.** {*;}
-keep class com.alibaba.** {*;}
-keep class com.alipay.** {*;}
-keep class com.ut.** {*;}
-keep class com.ta.** {*;}
-keep class anet.**{*;}
-keep class anetwork.**{*;}
-keep class org.android.spdy.**{*;}
-keep class org.android.agoo.**{*;}
-keep class android.os.**{*;}
-keep class org.json.**{*;}
-dontwarn com.taobao.**
-dontwarn com.alibaba.**
-dontwarn com.alipay.**
-dontwarn anet.**
-dontwarn org.android.spdy.**
-dontwarn org.android.agoo.**
-dontwarn anetwork.**
-dontwarn com.ut.**
-dontwarn com.ta.**

# 小米通道
-keep class com.xiaomi.** {*;}
-dontwarn com.xiaomi.**

# 华为通道
-keep class com.huawei.** {*;}
-dontwarn com.huawei.**

-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

# VIVO 通道
-keep class com.vivo.** {*;}
-dontwarn com.vivo.**

# OPPO 通道
-keep public class * extends android.app.Service

# 荣耀通道
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.hihonor.push.**{*;}`;
}

function addPermissions(manifest: any, permissionNames: string[]): void {
  if (!manifest['uses-permission']) {
    manifest['uses-permission'] = [];
  }

  const permissions = manifest['uses-permission'];

  permissionNames.forEach(permissionName => {
    const fullPermissionName = `android.permission.${permissionName}`;
    if (!permissions.some((perm: any) => perm.$['android:name'] === fullPermissionName)) {
      permissions.push({$: {'android:name': fullPermissionName}});
    }
  });
}

