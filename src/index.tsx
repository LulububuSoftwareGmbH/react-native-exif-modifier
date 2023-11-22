import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-exif-modifier' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const ExifModifier = NativeModules.ExifModifier
  ? NativeModules.ExifModifier
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function saveImageWithUserComment(
  base64ImageData: string,
  userComment: string
): Promise<string> {
  return ExifModifier.saveImageWithUserComment(base64ImageData, userComment);
}

export function saveImageAndModifyExif(
  base64ImageData: string,
  exifProperties: object
): Promise<string> {
  return ExifModifier.saveImageAndModifyExif(base64ImageData, exifProperties);
}
