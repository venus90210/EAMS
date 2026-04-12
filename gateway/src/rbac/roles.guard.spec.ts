import { Test, TestingModule } from '@nestjs/testing';
import { ExecutionContext, ForbiddenException } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { RolesGuard } from './roles.guard';
import { Request } from 'express';

describe('RolesGuard', () => {
  let guard: RolesGuard;
  let reflector: Reflector;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        RolesGuard,
        {
          provide: Reflector,
          useValue: {
            getAllAndOverride: jest.fn(),
          },
        },
      ],
    }).compile();

    guard = module.get<RolesGuard>(RolesGuard);
    reflector = module.get<Reflector>(Reflector);
  });

  describe('canActivate', () => {
    let context: ExecutionContext;
    let request: Partial<Request>;

    beforeEach(() => {
      request = {
        user: {
          userId: 'user-123',
          role: 'TEACHER',
          institutionId: 'inst-456',
        } as Express.User,
      };

      context = {
        switchToHttp: jest.fn().mockReturnValue({
          getRequest: jest.fn().mockReturnValue(request),
        }),
        getHandler: jest.fn(),
        getClass: jest.fn(),
      } as any;
    });

    it('should allow access when no @Roles() is specified', () => {
      (reflector.getAllAndOverride as jest.Mock).mockReturnValueOnce(undefined);

      const result = guard.canActivate(context);

      expect(result).toBe(true);
    });

    it('should allow access when user has required role', () => {
      (reflector.getAllAndOverride as jest.Mock).mockReturnValueOnce(['TEACHER']);

      const result = guard.canActivate(context);

      expect(result).toBe(true);
    });

    it('should deny access when user lacks required role', () => {
      (reflector.getAllAndOverride as jest.Mock).mockReturnValueOnce(['ADMIN']);

      expect(() => guard.canActivate(context)).toThrow(ForbiddenException);
      expect(() => guard.canActivate(context)).toThrow('INSUFFICIENT_ROLE');
    });

    it('should deny access when no user is attached', () => {
      (reflector.getAllAndOverride as jest.Mock).mockReturnValueOnce(['TEACHER']);
      (request as any).user = undefined;

      expect(() => guard.canActivate(context)).toThrow(ForbiddenException);
    });

    it('should allow access when user has one of multiple required roles', () => {
      (reflector.getAllAndOverride as jest.Mock).mockReturnValueOnce(['ADMIN', 'TEACHER']);

      const result = guard.canActivate(context);

      expect(result).toBe(true);
    });

    it('should deny access when user lacks all required roles', () => {
      (reflector.getAllAndOverride as jest.Mock).mockReturnValueOnce(['ADMIN', 'GUARDIAN']);

      expect(() => guard.canActivate(context)).toThrow(ForbiddenException);
    });

    it('should allow SUPERADMIN to access any role-restricted endpoint', () => {
      (request as any).user.role = 'SUPERADMIN';
      (reflector.getAllAndOverride as jest.Mock).mockReturnValueOnce(['ADMIN']);

      const result = guard.canActivate(context);

      expect(result).toBe(true);
    });
  });
});
