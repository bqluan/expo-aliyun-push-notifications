import {
  withInfoPlist,
  withAndroidManifest,
  AndroidConfig,
  ConfigPlugin,
} from 'expo/config-plugins';

interface AliPushConfig {
  aliPushAppKey: string;
  aliPushAppSecret: string;
}
const withMyApiKey: ConfigPlugin<AliPushConfig> = (config, { aliPushAppKey, aliPushAppSecret }) => {
  config = withInfoPlist(config, config => {
    config.modResults['Ali_Push_App_Key'] = aliPushAppKey;
    config.modResults['Ali_Push_App_Secret'] = aliPushAppSecret;
    return config;
  });

  config = withAndroidManifest(config, config => {
    const mainApplication = AndroidConfig.Manifest.getMainApplicationOrThrow(config.modResults);

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
    return config;
  });

  return config;
};

export default withMyApiKey;
