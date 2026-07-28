#!/bin/bash
sed -i 's/val ctcModelFile = files.find { it.name.startsWith("model") && it.name.endsWith(".onnx") }?.absolutePath ?: ""/val ctcModelFile = files.find { it.name == "model.onnx" || (it.name.startsWith("model") \&\& it.name.endsWith(".onnx")) }?.absolutePath ?: ""/g' app/src/main/java/com/example/ui/screens/SherpaModelFinder.kt

