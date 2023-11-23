# react-native-exif-modifier

`react-native-exif-modifier` is a React Native library that allows you to modify the EXIF data of an image and save the
changes. This can be particularly useful for applications that need to manage image metadata.

## Installation

To install the package, run:

```sh
npm install react-native-exif-modifier
```

## Usage

Import the functions from react-native-exif-modifier and use them to modify EXIF data in your images.

```js
import { saveImageWithUserComment, saveImageAndModifyExif } from 'react-native-exif-modifier';

// Save image with a user comment
const savedImagePath = await saveImageWithUserComment(base64ImageData, 'Your comment here');

// Modify and save image with new EXIF data
const modifiedImagePath = await saveImageAndModifyExif(base64ImageData, { /* EXIF properties */ });
```

## Functions

* `saveImageWithUserComment(base64ImageData: string, userComment: string): Promise<string>`
  * Saves the image with the provided user comment in the EXIF data.
* `saveImageAndModifyExif(base64ImageData: string, exifProperties: object): Promise<string>`
  * Saves the image with modified EXIF properties.

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
