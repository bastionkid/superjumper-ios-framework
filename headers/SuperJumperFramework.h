//
//  SuperJumperFramework.h
//  SuperJumperFramework
//
// Defines objc api for SuperJumperFramework implementation in RoboVM
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <UIKit/UIApplication.h>

@interface RoboGameContainer
+(RoboGameContainer*)newInstance:(NSString*)frameworkId classname:(NSString*)classname;
-(id)init;
-(void)viewWillAppear:(BOOL)animated;
-(void)viewDidAppear:(BOOL)animated;
-(void)viewWillDisappear:(BOOL)animated;
-(void)viewDidDisappear:(BOOL)animated;
@end
