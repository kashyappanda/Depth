import UIKit

class DepthImageFilters {
  
    var context: CIContext
  
    init(context: CIContext) {
        self.context = context
    }
  
    init() {
        context = CIContext()
    }
    
    func blur(image: CIImage) -> UIImage? {
        
        let output = image.applyingFilter("CIGaussianBlur", parameters: ["inputRadius": 3.0])
        
        guard let cgImage = context.createCGImage(output, from: output.extent) else {
            return nil
        }
        
        return UIImage(cgImage: cgImage)
    }
}
