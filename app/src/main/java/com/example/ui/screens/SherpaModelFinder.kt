package com.example.ui.screens

import java.io.File
import com.k2fsa.sherpa.onnx.*

object SherpaModelFinder {
    fun findModelRoot(dir: File): File? {
        if (!dir.exists() || !dir.isDirectory) return null
        
        val files = dir.listFiles() ?: return null
        
        val hasTokens = files.any { it.name == "tokens.txt" }
        val hasEncoder = files.any { it.name.startsWith("encoder") && it.name.endsWith(".onnx") }
        val hasDecoder = files.any { it.name.startsWith("decoder") && it.name.endsWith(".onnx") }
        val hasJoiner = files.any { it.name.startsWith("joiner") && it.name.endsWith(".onnx") }
        val hasModel = files.any { it.name == "model.onnx" || (it.name.startsWith("model") && it.name.endsWith(".onnx")) }
        val hasParaformerEncoder = files.any { it.name == "encoder.onnx" } && !hasJoiner
        
        if (hasTokens && (hasEncoder || hasModel || hasParaformerEncoder)) {
            return dir
        }
        
        for (file in files) {
            if (file.isDirectory) {
                val found = findModelRoot(file)
                if (found != null) {
                    return found
                }
            }
        }
        
        return null
    }

    fun buildConfig(modelDir: File): OnlineRecognizerConfig {
        val files = modelDir.listFiles() ?: emptyArray()
        val tokensFile = files.find { it.name == "tokens.txt" }?.absolutePath ?: ""
        
        val encoderFile = files.find { it.name.startsWith("encoder") && it.name.endsWith(".onnx") }?.absolutePath ?: ""
        val decoderFile = files.find { it.name.startsWith("decoder") && it.name.endsWith(".onnx") }?.absolutePath ?: ""
        val joinerFile = files.find { it.name.startsWith("joiner") && it.name.endsWith(".onnx") }?.absolutePath ?: ""
        val ctcModelFile = files.find { it.name == "model.onnx" || (it.name.startsWith("model") && it.name.endsWith(".onnx")) }?.absolutePath ?: ""
        
        if (tokensFile.isBlank()) {
            throw IllegalArgumentException("tokens.txt is missing in the model directory")
        }

        val transducerConfig = OnlineTransducerModelConfig()
        val zipformerCtcConfig = OnlineZipformer2CtcModelConfig()
        val paraformerConfig = OnlineParaformerModelConfig()
        
        var valid = false
        when {
            encoderFile.isNotBlank() && decoderFile.isNotBlank() && joinerFile.isNotBlank() -> {
                transducerConfig.encoder = encoderFile
                transducerConfig.decoder = decoderFile
                transducerConfig.joiner = joinerFile
                valid = true
            }
            ctcModelFile.isNotBlank() -> {
                zipformerCtcConfig.model = ctcModelFile
                valid = true
            }
            encoderFile.isNotBlank() && decoderFile.isNotBlank() && joinerFile.isBlank() -> {
                paraformerConfig.encoder = encoderFile
                paraformerConfig.decoder = decoderFile
                valid = true
            }
        }
        
        if (!valid) {
            throw IllegalArgumentException("Could not detect a valid Sherpa ONNX model configuration. Make sure encoder/decoder/joiner or model.onnx exists.")
        }

        return OnlineRecognizerConfig(
            featConfig = FeatureConfig(sampleRate = 16000, featureDim = 80),
            modelConfig = OnlineModelConfig(
                transducer = transducerConfig,
                zipformer2Ctc = zipformerCtcConfig,
                paraformer = paraformerConfig,
                tokens = tokensFile,
                numThreads = 4,
                debug = false
            ),
            endpointConfig = EndpointConfig(
                rule1 = EndpointRule(false, 2.4f, 0.0f),
                rule2 = EndpointRule(true, 1.4f, 0.0f),
                rule3 = EndpointRule(false, 0.0f, 20.0f)
            ),
            enableEndpoint = true,
        )
    }
}
