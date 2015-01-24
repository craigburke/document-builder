Groovy Document Builder
================
[ ![Codeship Status for craigburke/document-builder](https://codeship.com/projects/c4c04780-74d2-0132-8185-6662d475f668/status?branch=master)](https://codeship.com/projects/55079)

A document builder for Groovy for PDF or Word documents. This is still very much a work in progress.

**Dependencies:**

```groovy
compile 'com.craigburke.document:word:0.1.6'
compile 'com.craigburke.document:pdf:0.1.6'
```


**Example:**
```groovy
import com.craigburke.document.builder.WordDocumentBuilder
import com.craigburke.document.builder.PdfDocumentBuilder

WordDocumentBuilder builder = new WordDocumentBuilder(new File('myfile.docx'))
// PdfDocumentBuilder builder = new PdfDocumentBuilder(new File('myfile.pdf'))

builder.create { document(font: [family: 'Helvetica', size: 14.pt], margin: [top: 2.inches]) {
    paragraph "Hello World"
    
    // Each letter in this paragraph gets progressively bigger
    paragraph {
        text "look at this:"
        "HELLOOOOOOOOOO WORLD".each { letter ->
            font.size++
            text letter
        }
    }
    
    paragraph {
        text "Font size is back to 14pt now"
    }
    
    paragraph(margin: [left: 2.inches, right: 2.inches, top: 4.inches, bottom: 4.inches]) {
        font << [bold: true, color: '#333333']
        text "A paragraph with some margins"
    }
      
    paragraph {
        // add an image
        byte[] imageData = getClass().classLoader.getResource('cheeseburger.jpg').bytes
        image(data: imageData, width: 200.px, height: 250.px)
    }
      
    table(width: 5.inches , border: [size: 1, color: '#000000']) {
        row {
            cell("Cell 1", width: 1.inch)
            cell("Cell 2", width: 2.inches)
            cell(width: 2.inches) {
                text "Cell 3"
            }
        }
    }
        
}}
```
**Licences**
The core project as well as the Word document builder are available under the MPL2 license.
However, because of the use of iText, the PDF document builder is currently only available under a more restrictive AGPL license.

I'm currently looking into the possibility of developing a PDF builder that uses PdfBox as a dependency and could be made available under the same MPL2 licence as the rest of the project.
