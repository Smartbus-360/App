package com.smartbus360.app.data.olaMaps

import android.util.Log
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex

fun getSmartBusTileSource(): ITileSource = object : OnlineTileSourceBase(
    "SmartBus360Raster512_v3",
    0,
    15,   // match your tiles' maxzoom; raise if your data.json says higher
    512,  // 512px tiles
    ".png",
    arrayOf("https://map1.smartbus360.com/styles/basic-preview/")
) {
    override fun getTileURLString(index: Long): String {
        val z = MapTileIndex.getZoom(index)
        val x = MapTileIndex.getX(index)
        val y = MapTileIndex.getY(index)
        val url = "${baseUrl}$z/$x/$y@2x$mImageFilenameEnding" // retina 512px tiles
        Log.d("TileSource", "Loading tile from SmartBus360: $url")
        return url
    }
}
