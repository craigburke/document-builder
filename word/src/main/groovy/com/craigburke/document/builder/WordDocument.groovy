package com.craigburke.document.builder

import groovy.xml.StreamingMarkupBuilder

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Helper class for writing document in OOXML format
 * @author Craig Burke
 */
class WordDocument {

    private static final String ROOT_RELATIONSHIP_FILE = '_rels/.rels'
    private static final String CONTENT_FOLDER = 'word'
    private static final String IMAGE_FOLDER = 'media'

    private static final String XML_HEADER = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    private static final DOCUMENT_NAMESPACES = [
            w  : 'http://schemas.openxmlformats.org/wordprocessingml/2006/main',
            a  : 'http://schemas.openxmlformats.org/drawingml/2006/main',
            pic: 'http://schemas.openxmlformats.org/drawingml/2006/picture',
            wp : 'http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing',
            r  : 'http://schemas.openxmlformats.org/officeDocument/2006/relationships'
    ]

    Map<String, DocumentPart> documentParts = [:]
    List<ContentType> contentTypes = []
    ZipOutputStream zipStream
    List<ContentTypeOverride> contentTypeOverrides = []

    WordDocument(OutputStream out) {
        documentParts[BasicDocumentPartTypes.ROOT.value] = new DocumentPart(type: BasicDocumentPartTypes.ROOT)
        documentParts[BasicDocumentPartTypes.DOCUMENT.value] = new DocumentPart(type: BasicDocumentPartTypes.DOCUMENT)

        zipStream = new ZipOutputStream(out)
        addRelationship(
                "${CONTENT_FOLDER}/${BasicDocumentPartTypes.DOCUMENT.fileName}",
                'http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument',
                BasicDocumentPartTypes.ROOT
        )

        contentTypes << new ContentType(
                extension: 'rels',
                type: 'application/vnd.openxmlformats-package.relationships+xml')
        contentTypes << new ContentType(extension: 'xml', type: 'application/xml')
        contentTypes << new ContentType(extension: 'png', type: 'image/png')
        contentTypes << new ContentType(extension: 'jpg', type: 'image/jpeg')
        contentTypes << new ContentType(extension: 'jpeg', type: 'image/jpeg')
    }

    String addRelationship(String target, String type, DocumentPartType part) {
        def currentRelationships = documentParts[part.value].relationships
        String id = "rId${currentRelationships.size() + 1}"
        currentRelationships << new Relationship(id: id, target: target, type: type)
        id
    }

    void write() {
        writeDocPropsFiles()
        writeCustomFiles()
        writeRelationships()
        writeContentTypes()
        zipStream.close()
    }

    void writeDocPropsFiles() {
        writeZipEntry 'docProps/app.xml', 'application/vnd.openxmlformats-officedocument.extended-properties+xml', new StreamingMarkupBuilder().bind { builder ->
            mkp.yieldUnescaped(XML_HEADER)
            namespaces << ['': 'http://schemas.openxmlformats.org/officeDocument/2006/extended-properties']
            Properties {
                Application('Groovy Document Builder')
            }
        }

        writeZipEntry 'docProps/core.xml', 'conteapplication/vnd.openxmlformats-package.core-properties+xml', new StreamingMarkupBuilder().bind { builder ->
            mkp.yieldUnescaped(XML_HEADER)
            namespaces << [
                    ''       : 'http://schemas.openxmlformats.org/package/2006/metadata/core-properties',
                    'cp'     : 'http://schemas.openxmlformats.org/package/2006/metadata/core-properties',
                    'dc'     : 'http://purl.org/dc/elements/1.1/',
                    'dcterms': 'http://purl.org/dc/terms/',
                    'xsi'    : 'http://www.w3.org/2001/XMLSchema-instance'
            ]
            coreProperties {
                dc.creator('Groovy Document Builder')
            }
        }
    }

    void writeCustomFiles() {}

    protected writeZipEntry(String filePath, String contentType = null, Writable writable) {
        zipStream.putNextEntry(new ZipEntry(filePath))
        zipStream << writable
        zipStream.closeEntry()
        if (contentType) {
            contentTypeOverrides << new ContentTypeOverride(
                    partName: "/${filePath}",
                    contentType: contentType
            )
        }
    }

    def generateDocument(Closure documentClosure) {
        zipStream.putNextEntry(new ZipEntry("${CONTENT_FOLDER}/${BasicDocumentPartTypes.DOCUMENT.fileName}"))
        zipStream << new StreamingMarkupBuilder().bind { builder ->
            mkp.yieldUnescaped(XML_HEADER)
            namespaces << DOCUMENT_NAMESPACES
            documentClosure.delegate = builder
            documentClosure(builder)

        }.toString()
        zipStream.closeEntry()
        addImageFiles()
    }

    String generateDocumentPart(DocumentPartType type, Closure builderClosure) {
        documentParts[type.value] = new DocumentPart(type: type)

        zipStream.putNextEntry(new ZipEntry("${CONTENT_FOLDER}/${type.fileName}"))
        zipStream << new StreamingMarkupBuilder().bind { builder ->
            mkp.yieldUnescaped(XML_HEADER)
            namespaces << DOCUMENT_NAMESPACES
            builderClosure.delegate = builder
            builderClosure(builder)
        }.toString()
        zipStream.closeEntry()

        addRelationship(
                type.fileName,
                type.relationshipType,
                BasicDocumentPartTypes.DOCUMENT
        )
    }

    private addImageFiles() {
        documentParts.each { String name, DocumentPart part ->
            part.images.each { image ->
                zipStream.putNextEntry(new ZipEntry("${CONTENT_FOLDER}/${IMAGE_FOLDER}/${image.name}"))
                zipStream << image.data
                zipStream.closeEntry()
            }
        }

    }

    String addImage(String name, byte[] imageData, DocumentPartType partType) {
        String id = addRelationship(
                "${IMAGE_FOLDER}/${name}",
                'http://schemas.openxmlformats.org/officeDocument/2006/relationships/image',
                partType
        )
        documentParts[partType.value].images << [id: id, name: name, data: imageData]
        id
    }

    private void writeRelationships() {
        documentParts.each { String name, DocumentPart documentPart ->
            writeRelationshipsForPart(documentPart.type)
        }
    }

    private void writeRelationshipsForPart(DocumentPartType documentPart) {
        String fileLocation
        if (documentPart == BasicDocumentPartTypes.ROOT) {
            fileLocation = ROOT_RELATIONSHIP_FILE
        } else {
            fileLocation = "${CONTENT_FOLDER}/_rels/${documentPart.fileName}.rels"
        }

        zipStream.putNextEntry(new ZipEntry(fileLocation))
        zipStream << new StreamingMarkupBuilder().bind {
            mkp.yieldUnescaped(XML_HEADER)
            namespaces << ['': 'http://schemas.openxmlformats.org/package/2006/relationships']
            Relationships {
                documentParts[documentPart.value].relationships.each { Relationship relationship ->
                    Relationship(Id: relationship.id, Target: relationship.target, Type: relationship.type)
                }
            }
        }.toString()
        zipStream.closeEntry()
    }

    private void writeContentTypes() {
        zipStream.putNextEntry(new ZipEntry('[Content_Types].xml'))
        zipStream << new StreamingMarkupBuilder().bind {
            mkp.yieldUnescaped(XML_HEADER)
            namespaces << ['': 'http://schemas.openxmlformats.org/package/2006/content-types']
            Types {
                contentTypes.each { ContentType type ->
                    Default(Extension: type.extension, ContentType: type.type)
                }
                def nonRootParts = documentParts.findAll { it.key != BasicDocumentPartTypes.ROOT.value }
                nonRootParts.each { String name, DocumentPart documentPart ->
                    Override(PartName: "/${CONTENT_FOLDER}/${documentPart.type.fileName}",
                            ContentType: documentPart.type.contentType)
                }
                contentTypeOverrides.each { ContentTypeOverride override ->
                    Override(PartName: override.partName, ContentType: override.contentType)
                }
            }
        }.toString()
        zipStream.closeEntry()
    }

}
