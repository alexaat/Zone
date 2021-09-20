package com.alexaat.zone

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil


class Zone private constructor(
    val title: String,
    var location: LatLng,
    var radius: Double,
    val borderColor: Int,
    val borderWidth: Float,
    val fillColor: Int,
    val borderColorInactive: Int,
    val fillColorInactive: Int,
    val context: Context,
    val map: GoogleMap,
    val onCloseListener: ((Zone)->Unit)?,
    val onResizeCompleteListener: ((Zone)->Unit)?,
    val onDragCompleteListener: ((Zone)->Unit)?
) {

    private var marker: Marker? = null
    private var circle: Circle? = null
    private var markerResize: Marker? = null
    private var markerClose: Marker? = null
    private var resizeMarkerHeading = defaultResizeMarkerHeading
    private var isSelected = true

    init{
        setListeners(map)
    }

    class Builder(
        var title: String = "No title",
        var location: LatLng = LatLng(-33.865143,151.209900),
        var radius: Double = 35.0,
        var borderColor: Int = 0xAAFF0000.toInt(),
        var borderWidth: Float = 4.0f,
        var fillColor: Int = 0x44FF0000,
        var borderColorInactive: Int =  0x00000000,
        var fillColorInactive: Int = 0x11FF0000,
        var onCloseListener: ((Zone)->Unit)? = null,
        var onResizeCompleteListener: ((Zone)->Unit)? = null,
        var onDragCompleteListener: ((Zone)->Unit)? = null
    ){
        fun title(title: String) = apply { this.title = title }
        fun location(location: LatLng) = apply { this.location = location }
        fun radius(radius: Double) = apply { this.radius = radius }
        fun borderColor(borderColor: Int) = apply { this.borderColor = borderColor }
        fun borderWidth(borderWidth: Float) = apply { this.borderWidth = borderWidth }
        fun fillColor(fillColor: Int) = apply { this.fillColor = fillColor }
        fun borderColorInactive(borderColorInactive: Int) = apply { this.borderColorInactive = borderColorInactive }
        fun fillColorInactive(fillColorInactive: Int) = apply { this.fillColorInactive = fillColorInactive }
        fun onCloseListener(onCloseListener:(Zone)->Unit) = apply { this.onCloseListener = onCloseListener }
        fun onResizeCompleteListener(onResizeCompleteListener:(Zone)->Unit) = apply { this.onResizeCompleteListener = onResizeCompleteListener }
        fun onDragCompleteListener(onDragCompleteListener:(Zone)->Unit) = apply { this.onDragCompleteListener = onDragCompleteListener }
        fun build(context: Context, map: GoogleMap):Zone{
            return Zone(
                title = title,
                location= location,
                radius = radius,
                borderColor = borderColor,
                borderWidth = borderWidth,
                fillColor = fillColor,
                borderColorInactive = borderColorInactive,
                fillColorInactive = fillColorInactive,
                context = context,
                map = map,
                onCloseListener = onCloseListener,
                onResizeCompleteListener = onResizeCompleteListener,
                onDragCompleteListener = onDragCompleteListener)
        }
    }

    fun showOnMap(){
        zones.forEach {
            it.isSelected = false
            it.marker?.remove()
            it.circle?.remove()
            it.markerResize?.remove()
            it.markerClose?.remove()
        }
        this.isSelected = true
        zones.add(this)
        zones.forEach {
            it.addMarker()
            it.addCircle()
            if(it.isSelected){
                it.addResizeMarker()
                it.addCloseMarker()
            }
        }
    }
    fun remove(){
        remove(this)
    }

    fun setSelected(){
        setSelected(this)
    }

    private fun addMarker(){
        val markerOptions = MarkerOptions()
            .position(location)
            .title(title)
            .draggable(true)
        marker = map.addMarker(markerOptions)
    }
    private fun addCircle(){
        val color1 = if (isSelected) borderColor else borderColorInactive
        val color2 = if (isSelected) fillColor else fillColorInactive
        val circleOptions = CircleOptions()
            .center(LatLng(location.latitude, location.longitude))
            .radius(radius)
            .strokeColor(color1)
            .fillColor(color2)
            .strokeWidth(borderWidth)
        circle = map.addCircle(circleOptions)

    }

    private fun addResizeMarker(){
        val markerOptionsResizeLatLng = SphericalUtil.computeOffset(
            LatLng(location.latitude,location.longitude),
            radius,
            resizeMarkerHeading + map.cameraPosition.bearing
        )
        val rotation = resizeMarkerHeading.toFloat()  - defaultResizeMarkerHeading.toFloat()
        val markerOptionsResize =  MarkerOptions()
            .position(markerOptionsResizeLatLng)
            .title(title + resizeMarkerTitleSuffix)
            .draggable(true)
            .visible(isSelected)
            .icon(bitmapDescriptorFromVector(context, R.drawable.ic_resize))
            .rotation(rotation)
            .anchor(0.5f,0.5f)
        markerResize = map.addMarker(markerOptionsResize)
    }

    private fun addCloseMarker(){
        val icon = bitmapDescriptorFromVector(context, R.drawable.ic_close)

        val markerOptionsCloseLatLng = SphericalUtil.computeOffset(
            LatLng(location.latitude,location.longitude),
            radius,closeMarkerHeading + map.cameraPosition.bearing)
        val markerOptionsClose =  MarkerOptions()
            .position(markerOptionsCloseLatLng)
            .title(title + closeMarkerTitleSuffix)
            .visible(isSelected)
            .icon(icon)
            .anchor(0.5f,0.5f)


        markerClose = map.addMarker(markerOptionsClose)

    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    companion object{
        private const val defaultResizeMarkerHeading = 135.0
        private val resizeMarkerHeadingRange = 0.0..30.0
        private const val resizeMarkerTitleSuffix = "_resize"
        private const val closeMarkerTitleSuffix = "_close"
        private const val minimumRadius = 5.0
        private const val closeMarkerHeading = 45.0

        private var zones = mutableListOf<Zone>()
        private fun setListeners(map:GoogleMap){

            map.setOnCameraMoveListener{
                val zone = zones.find { it.isSelected }
                zone?.let{
                    it.apply{
                        markerClose?.remove()
                        markerResize?.remove()
                        addCloseMarker()
                        addResizeMarker()
                    }
                }
            }

            map.setOnMarkerClickListener { marker ->
                marker?.let{
                    if(marker.title.contains(closeMarkerTitleSuffix)){
                        val zone = zones.find { it.title == marker.title.removeSuffix(closeMarkerTitleSuffix) }
                        zone?.onCloseListener?.let{
                            it.invoke(zone)
                            return@setOnMarkerClickListener true
                        }
                        zone?.let{
                            remove(it)
                        }

                    }
                    val zone = zones.find { it.title == marker.title}
                    zone?.setSelected()
                    zone?.marker?.showInfoWindow()

                }
                true
            }

            map.setOnMarkerDragListener (object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragStart(marker: Marker?) {
                    marker?.let{ m->
                        if(!m.title.contains(resizeMarkerTitleSuffix)){
                            val selectedZone = zones.find{it.isSelected}
                            selectedZone?.let{
                                it.isSelected = false
                                it.circle?.remove()
                                it.markerClose?.remove()
                                it.markerResize?.remove()
                                it.addCircle()
                            }
                        }
                    }




                    val zone = zones.find { it.title == marker?.title?.removeSuffix(resizeMarkerTitleSuffix) }
                    zone?.isSelected = true

                    updateMarkers(marker)

                }

                override fun onMarkerDrag(marker: Marker?) {
                    updateMarkers(marker)
                }

                override fun onMarkerDragEnd(marker: Marker?) {
                    updateMarkers(marker)
                    checkResizeMarkerHeadingOverlap(marker)
                    executeOnResizeCompleteListener(marker)
                    executeOnDragCompleteListener(marker)
                }

            }


            )
        }

        private fun remove(zone: Zone){

            zone.marker?.remove()
            zone.circle?.remove()
            zone.markerResize?.remove()
            zone.markerClose?.remove()
            zones.remove(zone)
        }

        private fun updateMarkers(marker: Marker?){
            if(marker!=null){
                val zone = zones.find { it.title==marker.title.removeSuffix(resizeMarkerTitleSuffix) }
                zone?.let{
                    if(marker.title.contains(resizeMarkerTitleSuffix)){
                        var r = SphericalUtil.computeDistanceBetween(it.location, marker.position)
                        if (r < minimumRadius)
                            r = minimumRadius

                        val heading = SphericalUtil.computeHeading(it.location, marker.position) - zone.map.cameraPosition.bearing
                        val rotation = heading.toFloat() - defaultResizeMarkerHeading.toFloat()

                        it.apply {
                            radius = r
                            resizeMarkerHeading = heading
                            circle?.remove()
                            markerClose?.remove()
                            addCircle()
                            addCloseMarker()
                            markerResize?.rotation = rotation
                        }
                        return@let
                    }
                    it.apply {
                        circle?.remove()
                        markerResize?.remove()
                        markerClose?.remove()
                        location = LatLng(marker.position.latitude, marker.position.longitude)
                        addCircle()
                        addResizeMarker()
                        addCloseMarker()
                    }
                }

            }
        }
        private fun checkResizeMarkerHeadingOverlap(marker: Marker?){
            if(marker!=null){
                val zone = zones.find { it.title==marker.title.removeSuffix(resizeMarkerTitleSuffix) }
                zone?.let{
                    if(marker.title.contains(resizeMarkerTitleSuffix)){
                        var heading = SphericalUtil.computeHeading(it.location, marker.position)
                        val closeMarkerHeading = SphericalUtil.computeHeading(it.location, it.markerClose?.position)
                        val diff = Math.abs(heading-closeMarkerHeading)
                        if(diff in resizeMarkerHeadingRange){
                            heading = closeMarkerHeading + resizeMarkerHeadingRange.endInclusive/2
                            it.apply {
                                markerResize?.remove()
                                resizeMarkerHeading = heading - it.map.cameraPosition.bearing
                                addResizeMarker()

                            }

                        }

                    }
                }

            }
        }
        private fun setSelected(zone: Zone){

            zone?.let{ z->
                zones.forEach {
                    it.isSelected = false
                    it.marker?.remove()
                    it.circle?.remove()
                    it.markerResize?.remove()
                    it.markerClose?.remove()
                }
                z.isSelected = true
                zones.forEach {
                    it.addMarker()
                    it.addCircle()
                    if(it.isSelected){
                        it.addResizeMarker()
                        it.addCloseMarker()
                    }
                }
            }
        }
        private fun executeOnResizeCompleteListener(marker: Marker?){
            marker?.let{
                if(it.title.contains(resizeMarkerTitleSuffix)){
                    val zone = zones.find {z -> z.title==marker.title.removeSuffix(resizeMarkerTitleSuffix)}
                    zone?.onResizeCompleteListener?.invoke(zone)
                }
            }
        }
        private fun executeOnDragCompleteListener(marker: Marker?){
            marker?.let{
                if(!it.title.contains(resizeMarkerTitleSuffix) && !it.title.contains(closeMarkerTitleSuffix)){
                    val zone = zones.find { z -> z.title==marker.title.removeSuffix(resizeMarkerTitleSuffix)}
                    zone?.onDragCompleteListener?.invoke(zone)
                }
            }
        }
    }


}