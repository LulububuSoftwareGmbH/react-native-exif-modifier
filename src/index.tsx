import { NativeModules, Platform } from 'react-native';
import type { ImageProperties } from './constants/ImageProperties';

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

export function saveImageWithProperties(
  base64ImageData: string,
  properties: ImageProperties
): Promise<string> {
  return ExifModifier.saveImageWithProperties(base64ImageData, properties);
}
