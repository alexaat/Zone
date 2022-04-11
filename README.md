This library allows to add complex marker to google map for Android platform.
Complex marker consists of an icon, circle with radius, close button and resize button.
You can customize, move, resize and delete markers.
As this library uses map's setOnCameraMoveListener, setOnMarkerClickListener and setOnMarkerDragListener,
avoid using these listeners in your project.
Add library to project:

    Step 1: Add it in your root build.gradle at the end of repositories:
        allprojects {
            repositories {
                 ...
                maven { url 'https://jitpack.io' }
            }
        }
        
        Or in settings.gradle 
        
        dependencyResolutionManagement {
            repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                repositories {
                   google()
                   mavenCentral()
                   maven { url 'https://jitpack.io' }
                }
        }
        
    Step 2: Add the dependency
        dependencies {
              implementation 'com.github.alexaat:Zone:1.1.0'
        }


Implementation 1:
           val zone =  Zone.Builder()
                .title("Home")
                .location(LatLng(-33.8597747,151.2218797))
                .radius(35.0)
                .borderColor(ActivityCompat.getColor(this, R.color.blue))
                .fillColor(ActivityCompat.getColor(this, R.color.blueLight))
                .fillColorInactive(ActivityCompat.getColor(this, R.color.blueSuperLight))
                .onResizeCompleteListener{
                    Toast.makeText(applicationContext, "new radius is: ${it.radius}",Toast.LENGTH_SHORT).show()
                }
                .onDragCompleteListener{
                    Toast.makeText(applicationContext, "new location is: (${it.location.latitude}, ${it.location.longitude})",Toast.LENGTH_SHORT).show()
                }
                .onCloseListener{
                    Toast.makeText(applicationContext, "closing: ${it.title}",Toast.LENGTH_SHORT).show()
                    it.remove()
                }
                .icon(Zone.bitmapDescriptorFromVector(applicationContext,R.drawable.ic_home))
                .build(applicationContext, map)
                .showOnMap()


Implementation 2:
             Zone.Builder()
            .title("Work")
            .location(LatLng(-33.8567602,151.2152432))
            .radius(60.0)
            .onResizeCompleteListener{
                Toast.makeText(applicationContext, "new radius is: ${it.radius}",Toast.LENGTH_SHORT).show()
            }
            .onDragCompleteListener{
                Toast.makeText(applicationContext, "new location is: (${it.location.latitude}, ${it.location.longitude})",Toast.LENGTH_SHORT).show()
            }
            .onCloseListener{
                Toast.makeText(applicationContext, "closing: ${it.title}",Toast.LENGTH_SHORT).show()
                it.remove()
            }
            .icon(Zone.bitmapDescriptorFromVector(applicationContext,R.drawable.ic_work))
            .build(applicationContext, map)
            .showOnMap()


