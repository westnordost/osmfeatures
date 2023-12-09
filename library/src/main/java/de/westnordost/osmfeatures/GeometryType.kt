package de.westnordost.osmfeatures

enum class GeometryType {
    /** an OSM node that is not a member of any way  */
    POINT,

    /** an OSM node that is a member of one or more ways  */
    VERTEX,

    /** an OSM way that is not an area  */
    LINE,

    /** a OSM way that is closed/circular (the first and last nodes are the same) or a type=multipolygon relation  */
    AREA,

    /** an OSM relation  */
    RELATION
}
