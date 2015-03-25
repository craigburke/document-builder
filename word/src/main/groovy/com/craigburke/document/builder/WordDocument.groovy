package com.craigburke.document.builder

import groovy.xml.StreamingMarkupBuilder

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Helper class for writing document in OOXML format
 * @author Craig Burke
 */
class WordDocument {

    private static final String DOCUMENT_FILE = 'word/document.xml'
    private static final String IMAGE_PATH = 'word/media'
    private static final String RELATIVE_IMAGE_PATH = 'media'

    List<Relationship> relationships = []
    List<ContentType> contentTypes = []
    List images = []
    ZipOutputStream zipStream

    WordDocument(OutputStream out) {
        zipStream = new ZipOutputStream(out)
        addRelationship(
            DOCUMENT_FILE,
            'http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument',
            DocumentPart.MAIN
        )

        contentTypes << new ContentType(
                extension:'rels',
                type:'application/vnd.openxmlformats-package.relationships+xml')
        contentTypes << new ContentType(extension:'xml', type:'application/xml')
        contentTypes << new ContentType(extension:'png', type:'image/png')
    }

    String addRelationship(String target, String type, DocumentPart part) {
        String id = "rId${relationships.size() + 1}"
        relationships << new Relationship(id:id, target:target, type:type, part:part)
        id
    }

    void write() {
        writeRelationships()
        writeContentTypes()
        zipStream.close()
    }

    def generate(Closure documentClosure) {
        zipStream.putNextEntry(new ZipEntry(DOCUMENT_FILE))
        zipStream << new StreamingMarkupBuilder().bind { builder ->
            mkp.yieldUnescaped('<?xml version="1.0" encoding="UTF-16" standalone="yes"?>')
            namespaces << [
                    w:'http://schemas.openxmlformats.org/wordprocessingml/2006/main',
                    a:'http://schemas.openxmlformats.org/drawingml/2006/main',
                    pic:'http://schemas.openxmlformats.org/drawingml/2006/picture',
                    wp:'http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing',
                    r:'http://schemas.openxmlformats.org/officeDocument/2006/relationships'
            ]
            documentClosure.delegate = builder
            documentClosure(builder)

        }.toString().getBytes('UTF-16')
        zipStream.closeEntry()
        addImageFiles()
    }

    private addImageFiles() {
        images.each { image ->
            zipStream.putNextEntry(new ZipEntry("${IMAGE_PATH}/${image.name}"))
            zipStream << image.data
            zipStream.closeEntry()
        }
    }

    String addImage(String name, byte[] imageData) {
        String id = addRelationship(
                "${RELATIVE_IMAGE_PATH}/${name}",
                'http://schemas.openxmlformats.org/officeDocument/2006/relationships/image',
                DocumentPart.DOCUMENT
        )
        images << [id:id, name:name, data:imageData]
        id
    }

    private void writeRelationships() {
        writeRelationshipsForPart(DocumentPart.MAIN, '_rels/.rels')
        writeRelationshipsForPart(DocumentPart.DOCUMENT, 'word/_rels/document.xml.rels')
    }

    private void writeRelationshipsForPart(DocumentPart documentPart, String fileName) {
        zipStream.putNextEntry(new ZipEntry(fileName))
        zipStream << new StreamingMarkupBuilder().bind {
            mkp.yieldUnescaped('<?xml version="1.0" encoding="UTF-16" standalone="yes"?>')
            namespaces << ['':'http://schemas.openxmlformats.org/package/2006/relationships']
            Relationships {
                relationships.findAll { it.part == documentPart }.each { Relationship relationship ->
                    Relationship(Id:relationship.id, Target:relationship.target, Type:relationship.type)
                }
            }
        }.toString().getBytes('UTF-16')
        zipStream.closeEntry()
    }

    private void writeContentTypes() {
        zipStream.putNextEntry(new ZipEntry('[Content_Types].xml'))
        zipStream << new StreamingMarkupBuilder().bind {
            mkp.yieldUnescaped('<?xml version="1.0" encoding="UTF-16" standalone="yes"?>')
            namespaces << ['':'http://schemas.openxmlformats.org/package/2006/content-types']
            Types {
                contentTypes.each { ContentType type ->
                    Default(Extension:type.extension, ContentType:type.type)
                }
                Override(PartName:"/${DOCUMENT_FILE}",
                        ContentType:'application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml')
            }
        }.toString().getBytes('UTF-16')
        zipStream.closeEntry()
    }

}
