import os


class Preset:
    subtitles: bool
    subtitlesLanguage: str
    subtitleFormat: str
    archive: str
    thumbnails: bool
    cutThumbnails: bool
    saveOriginalThumbnail: bool
    format: str
    outputTemplate: str
    extractAudio: bool
    outputFileType: str = None


audioPreset = Preset()


audioPreset.subtitles = True
audioPreset.subtitlesLanguage = ["en.*"]
audioPreset.subtitleFormat = "vtt"
audioPreset.archive = os.path.join(os.environ["HOME"], "audioArchive.txt")
audioPreset.thumbnails = True
audioPreset.cutThumbnails = True
audioPreset.saveOriginalThumbnail = True
audioPreset.format = "bestaudio"
audioPreset.outputTemplate = "%(playlist_title)s/%(title)s.%(ext)s"
audioPreset.extractAudio = True
