import mutagen
import os


class Tagger:
    def checkIfFileOpen(self, filepath):
        try:
            file = mutagen.File(filepath)
            file.tags
            file.save()

            return 1

        except FileNotFoundError:
            return -1

        except (PermissionError, AttributeError, mutagen.MutagenError):
            return 0

    def setTags(self, videoInfo: dict):
        print("opening mutagen file")
        print(videoInfo['filepath'])

        file = mutagen.File(videoInfo['filepath'])

        file.tags['title'] = videoInfo.get('title')

        if videoInfo.get('album') is not None:
            file.tags['album'] = videoInfo.get('album')
            print("setting album to", videoInfo.get('album'))

        if videoInfo.get('artist') is not None:
            file.tags['artist'] = videoInfo.get('artist')
            print("setting artist to", videoInfo.get('artist'))

        if videoInfo.get('release_date') is not None:
            date = videoInfo.get('release_date')
            file.tags['date'] = date[0:4]+'-'+date[4:6]+'-'+date[6:8]
            print("setting release_date to",
                  date[0:4]+'-'+date[4:6]+'-'+date[6:8])

        if videoInfo.get('playlist_index') is not None:
            file.tags['tracknumber'] = str(videoInfo.get('playlist_index'))
            print("setting tracknumber to",
                  str(videoInfo.get('playlist_index')))

        lyricsFile = self.findLyricsFile(videoInfo['filepath'])

        if lyricsFile is not None:
            print("found lyrics", lyricsFile)
            lyrics = self.getLyricsData(lyricsFile)
            file.tags['lyrics'] = lyrics
        else:
            print("lyrics not found")

        print("saving tags")
        file.save()

    songExts = ['opus', 'mp3', 'mp4']
    lyricsExts = ['lrc']

    def findLyricsFile(self, songPath: str):
        songFilename = self.getFilename(songPath)
        folder = self.getFileDirectory(songPath)

        print(songFilename, folder)

        songFilenameNoExt = self.getFilenameNoExt(songFilename)

        for filename in os.listdir(folder):
            if filename.find(songFilenameNoExt) > -1:
                if self.checkIfLyricsExt(filename):
                    return os.path.join(folder, filename)

        return None

    def getFilenameNoExt(self, filename: str):
        filenameNoExt: str = ""

        dot: bool = False
        for char in filename[::-1]:
            if dot:
                filenameNoExt += char
            if char == ".":
                dot = True

        return filenameNoExt[::-1]

    def checkIfLyricsExt(self, filename: str):
        for ext in self.lyricsExts:
            if filename.find(ext, len(filename)-4) > -1:
                return True

        return False

    def getLyricsData(self, lyricsFilepath: str):
        lyricsFile = open(lyricsFilepath, 'rb')
        lyrics = lyricsFile.read()
        lyricsFile.close()

        return lyrics.decode()

    def getFileDirectory(self, filepath: str):
        splitPath = filepath.split(os.sep)

        directory = "/"

        for i, part in enumerate(splitPath):
            if i == len(splitPath) - 1:
                break

            directory = os.path.join(directory, part)

        return directory

    def getFilename(self, filepath: str):
        return filepath.split(os.sep)[-1]


# print(Tagger.getFileDirectory("/asdasd/a/sda/sd/asd/direc/file"))
# print(Tagger.getFilename("/asdasd/a/sda/sd/asd/direc/file"))
