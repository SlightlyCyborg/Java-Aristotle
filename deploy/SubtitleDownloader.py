from youtube_transcript_api import YouTubeTranscriptApi
from youtube_transcript_api.formatters import SRTFormatter

# function to get transcript for a given YouTube video id
def getTranscript(video_id):
    # code to get transcript
    return YouTubeTranscriptApi.get_transcript(video_id)

formatter = SRTFormatter()

# function to format transcript for easier reading
def formatTranscript(transcript):
    # code to format transcript
    print("transcript")
    print(transcript)
    return formatter.format_transcript(transcript)

def main():
    import sys
    import os
    import getopt
    
    # parse command line arguments
    try:
        opts, args = getopt.getopt(sys.argv[1:], "i:o:")
        
        # set up variables for input file and output directory
        for opt, arg in opts:
            if opt == '-i':
                input_file = arg
            elif opt == '-o':
                output_dir = arg
        # check that both input and output are specified
        if input_file is None or output_dir is None:
            raise getopt.GetoptError("Please specify both an input file and an output directory")
    except getopt.GetoptError:
        print("Must specify both an input file and an output directory")
        sys.exit(2)
    
    # create output directory if it doesn't exist
    if not os.path.exists(output_dir):
        os.mkdir(output_dir)
    
    # read input file line by line
    with open(input_file, 'r') as f:
        for line in f:
            # get transcript for each video
            video_id = line.strip()
            transcript = getTranscript(video_id)
            # format transcript
            formatted_transcript = formatTranscript(transcript)
            # write formatted transcript to output directory
            output_file_name = video_id + '.srt'
            with open(os.path.join(output_dir, output_file_name), 'w') as out_file:
                out_file.write(formatted_transcript)


if __name__ == '__main__':
    main()
