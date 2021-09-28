//
//  SuperJumperFramework.h
//  SuperJumperFramework
//
// Defines objc api for SuperJumperFramework implementation in RoboVM
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <UIKit/UIApplication.h>

//
// SuperJumperFrameworkDemo class with basic API demonstration
//
@interface SuperJumperFrameworkDemo
-(id)init;
-(id)initWithText:(NSString*)text;
+(void)hello;
-(NSString*)roboVmVersion;
-(void)installSignals:(void(^)(void))installer;
@end

// IOSGameLauncher class with basic API demonstration
@interface IOSGameLauncher
+(IOSGameLauncher*)instance;
-(id)init;
-(BOOL)didFinishLaunching:(UIApplication*)application;
-(void)didBecomeActive:(UIApplication*)application;
-(void)willEnterForeground:(UIApplication*)application;
-(void)willResignActive:(UIApplication*)application;
-(void)willTerminate:(UIApplication*)application;
+(void)hello;
@end
