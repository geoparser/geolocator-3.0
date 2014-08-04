CrossLingualV1.0
================
LanguageBridge v1.0 is a cross-lingual geoparser tool. You can input sentences in different languages (eg. Chinese, English, Arabic, Russian, Ukrainian, Indonesian) into LanguageBridge, and get the a location in the language that you set.
LanguageBridge can run on Windows, Mac, or Linux/Unix platforms, but you have to install a JDK ( recommended JDK1.7 ). If you have installed eclipse (recommended Kepler 64bit ), we can import the project directly.
/////////////// How to Install ///////////////
1. Install JDK and eclipse.
2. Access the https://github.com/geoparser, you will find 2 project, CrossLingualV1.0, geolocatorV3.0 and download them.
2. Import 2 project, CrossLingualV1.0, geolocatorV3.0, you should refer to geolocatorV3.0.
3. Run the main class "DemoCrossLingual3" in package cmu.edu.cn, before you run the main program, you should adjust the VM parameter (recommended -Xmx3000m), change the encoding(UTF8)  and set the path of GazIndex in Geoparse .java(cmu.edu.cn). The folder of GazIndex is in geolocator3.0, you should copy the Geonames, res and wordnet into root directory of CrossLingualV1.0.
4. After the gui is come out, you can choose language, the types of location, and translate tools (for Chinese recommended Microsoft, and Arabic, Russian, Ukrainian, Indonesian recommended Google),  input source sentences into left panel, press the button "Translate Source", after a few seconds, when the translation result comes out in right panel, then press the button "Geoparse Translation"
5 If you choose coordinate&confidence, you should translate it again (Translate Source) and press the Geoparse Translation button.
5. The final output come out in right panel.

please send email to cxsklse@gmail.com or gelern@cs.cmu.edu if you find any bug or have any question, or any suggestions.

