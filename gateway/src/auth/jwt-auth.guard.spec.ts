import { Test, TestingModule } from '@nestjs/testing';
import { ExecutionContext } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { JwtAuthGuard } from './jwt-auth.guard';
import { JwtService } from '@nestjs/jwt';

describe('JwtAuthGuard', () => {
  let guard: JwtAuthGuard;
  let reflector: Reflector;
  let jwtService: JwtService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        JwtAuthGuard,
        {
          provide: Reflector,
          useValue: {
            getAllAndOverride: jest.fn(),
          },
        },
        {
          provide: JwtService,
          useValue: {
            verify: jest.fn(),
          },
        },
      ],
    }).compile();

    guard = module.get<JwtAuthGuard>(JwtAuthGuard);
    reflector = module.get<Reflector>(Reflector);
    jwtService = module.get<JwtService>(JwtService);
  });

  describe('canActivate', () => {
    let context: ExecutionContext;

    beforeEach(() => {
      context = {
        getHandler: jest.fn(),
        getClass: jest.fn(),
        switchToHttp: jest.fn().mockReturnValue({
          getRequest: jest.fn(),
        }),
      } as any;
    });

    it('should allow access when route is marked as @Public()', () => {
      (reflector.getAllAndOverride as jest.Mock).mockReturnValueOnce(true);

      const result = guard.canActivate(context);

      expect(result).toBe(true);
    });

    it('should proceed with JWT validation when route is not public', () => {
      (reflector.getAllAndOverride as jest.Mock).mockReturnValueOnce(false);

      const request = {
        headers: {
          authorization: 'Bearer valid.jwt.token',
        },
      };

      (context.switchToHttp as jest.Mock).mockReturnValueOnce({
        getRequest: jest.fn().mockReturnValueOnce(request),
      });

      (jwtService.verify as jest.Mock).mockReturnValueOnce({
        sub: 'user-123',
        role: 'TEACHER',
      });

      const result = guard.canActivate(context);

      expect(result).toBeDefined();
    });

    it('should reject when authorization header is missing', () => {
      (reflector.getAllAndOverride as jest.Mock).mockReturnValueOnce(false);

      const request = {
        headers: {},
      };

      (context.switchToHttp as jest.Mock).mockReturnValueOnce({
        getRequest: jest.fn().mockReturnValueOnce(request),
      });

      expect(() => guard.canActivate(context)).toThrow();
    });
  });
});
