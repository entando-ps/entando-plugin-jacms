<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="/aps-core" prefix="wp" %>
<%@ taglib uri="/apsadmin-core" prefix="wpsa" %>
<%@ taglib prefix="jacms" uri="/jacms-apsadmin-core" %>
<%@ taglib prefix="wpsf" uri="/apsadmin-form" %>

<div class="modal fade bs-cropping-modal" tabindex="-1" role="dialog" aria-labelledby="myLargeModalLabel">
    <div class="modal-dialog modal-xlg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    Close
                    <span class="fa fa-times"></span>
                </button>
                <h4 class="modal-title">Edit Image <span class="image-name"></span></h4>
            </div>
            <div class="container-fluid no-padding">
                <div class="row">
                    <div class="col-md-8">
                        <!-- Tab panes -->
                        <div class="tab-content">
                            <!-- tab pane blue print -->
                            <div class="tab-pane hidden" id="tab-pane-blueprint">
                                <div class="container-fluid">
                                    <div class="row">
                                        <div class="col-md-8">
                                            <div class="image-container">
                                                <img src="" alt="" class="store_item_">
                                            </div>
                                        </div>
                                        <div class="col-md-4">
                                            <div class="docs-preview clearfix">
                                                <div class="img-preview preview-lg"><img
                                                        src="">
                                                </div>
                                                <div class="img-preview preview-md"><img
                                                        src="">
                                                </div>
                                                <div class="img-preview preview-sm"><img
                                                        src="">
                                                </div>
                                                <div class="img-preview preview-xs"><img
                                                        src="">
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row flex-container">
                                        <div class="col-md-8">
                                            <div class="toolbar-container flex-container space-between">
                                                <!-- move and crop -->
                                                <div class="btn-group flex-item">
                                                    <span class="btn-group__title">move</span>
                                                    <button type="button" class="btn btn-primary"
                                                            data-method="setDragMode" data-option="move"
                                                            title="Move">
                                                        <span class="docs-tooltip" data-toggle="tooltip" title=""
                                                              data-original-title="cropper.setDragMode(&quot;move&quot;)">
                                                            <span class="fa fa-arrows"></span>
                                                        </span>
                                                    </button>
                                                </div>
                                                <div class="divider flex-item"></div>

                                                <div class="btn-group flex-item">
                                                    <span class="btn-group__title">crop</span>
                                                    <button type="button" class="btn btn-primary"
                                                            data-method="setDragMode" data-option="crop"
                                                            title="Crop">
                                                        <span class="docs-tooltip" data-toggle="tooltip" title=""
                                                              data-original-title="cropper.setDragMode(&quot;crop&quot;)">
                                                            <span class="fa fa-crop"></span>
                                                        </span>
                                                    </button>
                                                </div>
                                                <div class="divider flex-item"></div>


                                                <!-- scale -->
                                                <div class="btn-group flex-item">
                                                    <span class="btn-group__title">scale</span>
                                                    <button type="button" class="btn btn-primary"
                                                            data-method="scaleX" data-option="-1"
                                                            title="Flip Horizontal">
                                                        <span class="docs-tooltip" data-toggle="tooltip" title=""
                                                              data-original-title="cropper.scaleX(-1)">
                                                            <span class="fa fa-arrows-h"></span>
                                                        </span>
                                                    </button>
                                                    <button type="button" class="btn btn-primary"
                                                            data-method="scaleY" data-option="-1"
                                                            title="Flip Vertical">
                                                        <span class="docs-tooltip" data-toggle="tooltip" title=""
                                                              data-original-title="cropper.scaleY(-1)">
                                                            <span class="fa fa-arrows-v"></span>
                                                        </span>
                                                    </button>
                                                </div>
                                                <div class="divider flex-item"></div>


                                                <!-- move -->
                                                <div class="btn-group flex-item">
                                                    <span class="btn-group__title">move</span>
                                                    <button type="button" class="btn btn-primary" data-method="move"
                                                            data-option="-10" data-second-option="0"
                                                            title="Move Left">
                                                        <span class="docs-tooltip" data-toggle="tooltip" title=""
                                                              data-original-title="cropper.move(-10, 0)">
                                                            <span class="fa fa-arrow-left"></span>
                                                        </span>
                                                    </button>
                                                    <button type="button" class="btn btn-primary" data-method="move"
                                                            data-option="10" data-second-option="0"
                                                            title="Move Right">
                                                        <span class="docs-tooltip" data-toggle="tooltip" title=""
                                                              data-original-title="cropper.move(10, 0)">
                                                            <span class="fa fa-arrow-right"></span>
                                                        </span>
                                                    </button>
                                                    <button type="button" class="btn btn-primary" data-method="move"
                                                            data-option="0" data-second-option="-10"
                                                            title="Move Up">
                                                        <span class="docs-tooltip" data-toggle="tooltip" title=""
                                                              data-original-title="cropper.move(0, -10)">
                                                            <span class="fa fa-arrow-up"></span>
                                                        </span>
                                                    </button>
                                                    <button type="button" class="btn btn-primary" data-method="move"
                                                            data-option="0" data-second-option="10"
                                                            title="Move Down">
                                                        <span class="docs-tooltip" data-toggle="tooltip" title=""
                                                              data-original-title="cropper.move(0, 10)">
                                                            <span class="fa fa-arrow-down"></span>
                                                        </span>
                                                    </button>
                                                </div>
                                                <div class="divider flex-item"></div>


                                                <!-- rotate -->
                                                <div class="btn-group flex-item">
                                                    <span class="btn-group__title">rotate</span>
                                                    <button type="button" class="btn btn-primary"
                                                            data-method="rotate"
                                                            data-option="-45" title="Rotate Left">
                                                        <span class="docs-tooltip" data-toggle="tooltip" title=""
                                                              data-original-title="cropper.rotate(-45)">
                                                            <span class="fa fa-rotate-left"></span>
                                                        </span>
                                                    </button>
                                                    <button type="button" class="btn btn-primary"
                                                            data-method="rotate"
                                                            data-option="45" title="Rotate Right">
                                                        <span class="docs-tooltip" data-toggle="tooltip" title=""
                                                              data-original-title="cropper.rotate(45)">
                                                            <span class="fa fa-rotate-right"></span>
                                                        </span>
                                                    </button>
                                                </div>
                                                <div class="divider flex-item"></div>


                                                <!-- zoom -->
                                                <div class="btn-group flex-item">
                                                    <span class="btn-group__title">zoom</span>
                                                    <button type="button" class="btn btn-primary" data-method="zoom"
                                                            data-option="0.1" title="Zoom In">
                                                        <span class="docs-tooltip" data-toggle="tooltip" title=""
                                                              data-original-title="cropper.zoom(0.1)">
                                                            <span class="fa fa-search-plus"></span>
                                                        </span>
                                                    </button>
                                                    <button type="button" class="btn btn-primary" data-method="zoom"
                                                            data-option="-0.1" title="Zoom Out">
                                                        <span class="docs-tooltip" data-toggle="tooltip" title=""
                                                              data-original-title="cropper.zoom(-0.1)">
                                                            <span class="fa fa-search-minus"></span>
                                                        </span>
                                                    </button>
                                                </div>
                                                <div class="divider flex-item"></div>


                                                <!-- save and cancel -->
                                                <div class="btn-group flex-item">
                                                    <span class="btn-group__title">save</span>
                                                    <button type="button" class="btn btn-primary" data-method="crop"
                                                            title="Crop">
                                                        <span class="docs-tooltip" data-toggle="tooltip" title=""
                                                              data-original-title="cropper.crop()">
                                                            <span class="fa fa-check"></span>
                                                        </span>
                                                    </button>
                                                </div>
                                                <div class="divider flex-item"></div>

                                                <div class="btn-group flex-item">
                                                    <span class="btn-group__title">cancel</span>
                                                    <button type="button" class="btn btn-primary"
                                                            data-method="remove"
                                                            title="Remove">
                                                        <span class="docs-tooltip" data-toggle="tooltip" title=""
                                                              data-original-title="cropper.clear()">
                                                            <span class="fa fa-remove"></span>
                                                        </span>
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-md-4 aspect-ratio-buttons-container ">
                                            <div class="aspect-ratio-buttons">
                                                <div class="btn-group d-flex flex-nowrap" data-toggle="buttons">
                                                    <label class="btn btn-primary active"
                                                           data-method="setAspectRatio" data-option="NaN">
                                                        <input type="radio" class="sr-only" id="aspectRatio5"
                                                               name="aspectRatio" value="NaN">
                                                        <span class="docs-tooltip" data-toggle="tooltip" title=""
                                                              data-original-title="aspectRatio: NaN">Free</span>
                                                    </label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <!-- /tab pane blue print -->
                        </div>
                    </div>
                    <div class="col-md-4">
                        <!-- Nav tabs -->
                        <ul class="nav nav-tabs tabs-left image-navigation">
                            <li>List of images</li>

                        </ul>
                        <!-- image navigation item blueprint -->
                        <li class="active image-navigation-item hidden" id="image-navigation-item-blueprint">
                            <a href="#first" data-toggle="tab">Blueprint</a></li>
                        <!-- /image navigation item blueprint -->
                    </div>
                </div>
            </div>
        </div>


        <div id="aspect-ratio-values">
            <wp:info key="systemParam" paramName="aspect_ratio"/>
        </div>


    </div>
</div>