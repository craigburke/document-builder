Groovy Document Builder
================
[ ![Codeship Status for craigburke/document-builder](https://codeship.com/projects/c4c04780-74d2-0132-8185-6662d475f668/status?branch=master)](https://codeship.com/projects/55079)

A document builder for Groovy for PDF or Word documents. This is still very much a work in progress.

**Dependencies:**

```
compile 'com.craigburke.document:word:0.1.1'
compile 'com.craigburke.document:pdf:0.1.1'
```


**Example:**
```
import com.craigburke.document.builder.WordDocumentBuilder
import com.craigburke.document.builder.PdfDocumentBuilder

WordDocumentBuilder builder = new WordDocumentBuilder('myfile.docx')
// or PdfDocumentBuilder builder = new PdfDocumentBuilder('myfile.pdf')

builder.document(font: [family: 'Helvetica', size: 14], marginTop: 144) {
    paragraph "Hello World"
    
    // A more interesting version with each letter getting progressively bigger
    paragraph {
        text "look at this:"
        "HELLOOOOOOOOOO WORLD".each { letter ->
            font.size++
            text letter
        }
    }
        
    paragraph(marginLeft: 144, marginRight: 144, marginTop: 288, marginBottom: 288) {
        text "A paragraph with some margins"
    }
      
    // add an image
    byte[] imageData = getClass().classLoader.getResource('cheeseburger.jpg').bytes
        
    paragraph {
        image(data: imageData, width: 200, height: 250)
    }
      
    table {
        row {
            cell("Cell 1")
            cell("Cell 2")
            cell {
                text "Cell 3"
            }
        }
    }
        
}
```
