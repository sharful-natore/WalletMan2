fun isVoskDownloaded(context: Context): Boolean {
    val modelDir = File(context.filesDir, "sherpa-model-bn")
    return SherpaModelFinder.findModelRoot(modelDir) != null
}
