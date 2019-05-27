<div class="container" id="search-container">
    <#list videos as video >
        <div class="row search-result-row">
            <div class="search-result-thumbnail col-md-4">
                <img src="${video.thumbnail}" id="img-${video.id}" class="thumbnail" data-video-id="${video.id}">
                <div class="video-wrapper" id="video-wrapper-${video.id}" data-video-id = "${video.id}">
                    <div class="video" id="video-${video.id}">
                    </div>
                </div>
            </div>
            <div class="search-result-title col-md-8">
                <div class="container">
                    <div class="container">
                        <h3>${video.title}</h3>
                    </div>
                    <div class="row">
                        <div>
                            <#assign x=0>
                            <#list video.blocks as block>
                                <div class="block-div">
                                    <a href="https://youtu.be/${video.id}?t=${block.startTime.forYTURL}"
                                       data-video-id="${video.id}"
                                    data-x="${x}"
                                    class="player-control player-control-for-${video.id}"
                                    data-time="${block.startTime.forJSPlayer}">
                                        ${block.startTime.forDisplay} - ${block.stopTime.forDisplay}
                                    </a>
                                    <div id="loader-${video.id}-${x}" style="display:none;">foo</div>
                                    <br>
                                    "${block.words}"
                                </div>
                                <#assign x=x+1>
                            </#list>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </#list>
    <div class="container">
       <div class="row"></div>
    </div>
</div>