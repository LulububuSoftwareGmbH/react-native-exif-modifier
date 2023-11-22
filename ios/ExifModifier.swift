import Foundation
import UIKit
import ImageIO
import Photos

@objc(ExifModifier)
class ExifModifier: NSObject {
    @objc func saveImageWithUserComment(_ base64ImageData: String, userComment: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        let properties: NSDictionary = [
            kCGImagePropertyExifDictionary as String: [
                kCGImagePropertyExifUserComment as String: userComment
            ]
        ]

        saveImageAndModifyExif(base64ImageData: base64ImageData, properties: properties, resolve: resolve, reject: reject)
    }

    @objc func saveImageAndModifyExif(base64ImageData: String, properties: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        DispatchQueue.global(qos: .background).async {
            guard let imageData = Data(base64Encoded: base64ImageData) else {
                reject("E_IMAGE_DATA", "Invalid image data", nil)
                return
            }

            guard let imageSource = CGImageSourceCreateWithData(imageData as CFData, nil) else {
                reject("E_IMAGE_SOURCE", "Could not create image source", nil)
                return
            }

            let uti = CGImageSourceGetType(imageSource)!
            let dataWithExif = NSMutableData(data: imageData)
            guard let destination = CGImageDestinationCreateWithData(dataWithExif, uti, 1, nil) else {
                reject("E_IMAGE_DESTINATION", "Could not create image destination", nil)
                return
            }

            CGImageDestinationAddImageFromSource(destination, imageSource, 0, properties)

            if !CGImageDestinationFinalize(destination) {
                reject("E_IMAGE_FINALIZE", "Failed to finalize image with EXIF data", nil)
                return
            }

            var localIdentifier: String?
            PHPhotoLibrary.shared().performChanges({
                let creationRequest = PHAssetCreationRequest.forAsset()
                creationRequest.addResource(with: .photo, data: dataWithExif as Data, options: nil)
                localIdentifier = creationRequest.placeholderForCreatedAsset?.localIdentifier
            }, completionHandler: { success, error in
                DispatchQueue.main.async {
                    if let error = error {
                        reject("E_PHOTO_LIBRARY", "Could not save image to photo library", error)
                    } else {
                        let base64String = dataWithExif.base64EncodedString()

                        resolve(base64String)
                    }
                }
            })
        }
    }
}
