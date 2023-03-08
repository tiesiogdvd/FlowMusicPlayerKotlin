import mutagen


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

        print("saving tags")
        file.save()
