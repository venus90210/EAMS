import { Test, TestingModule } from '@nestjs/testing';
import { ExecutionContext, BadRequestException, NotFoundException, ForbiddenException, ConflictException } from '@nestjs/common';
import { ErrorNormalizerInterceptor } from './error-normalizer.interceptor';
import { of, throwError } from 'rxjs';
import { AxiosError } from 'axios';

describe('ErrorNormalizerInterceptor', () => {
  let interceptor: ErrorNormalizerInterceptor;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [ErrorNormalizerInterceptor],
    }).compile();

    interceptor = module.get<ErrorNormalizerInterceptor>(ErrorNormalizerInterceptor);
  });

  describe('intercept', () => {
    let context: ExecutionContext;

    beforeEach(() => {
      context = {} as ExecutionContext;
    });

    it('should normalize 400 Bad Request error', (done) => {
      const axiosError = {
        response: {
          status: 400,
          data: { error: 'VALIDATION_ERROR', message: 'Invalid input' },
        },
      } as any;

      const next = {
        handle: jest.fn().mockReturnValueOnce(throwError(() => axiosError)),
      };

      interceptor.intercept(context, next).subscribe({
        next: () => {
          fail('Should have thrown error');
        },
        error: (error) => {
          expect(error).toBeInstanceOf(BadRequestException);
          done();
        },
      });
    });

    it('should normalize 404 Not Found error', (done) => {
      const axiosError = {
        response: {
          status: 404,
          data: { error: 'NOT_FOUND', message: 'Resource not found' },
        },
      } as any;

      const next = {
        handle: jest.fn().mockReturnValueOnce(throwError(() => axiosError)),
      };

      interceptor.intercept(context, next).subscribe({
        next: () => {
          fail('Should have thrown error');
        },
        error: (error) => {
          expect(error).toBeInstanceOf(NotFoundException);
          done();
        },
      });
    });

    it('should normalize 403 Forbidden error', (done) => {
      const axiosError = {
        response: {
          status: 403,
          data: { error: 'INSUFFICIENT_ROLE', message: 'Access denied' },
        },
      } as any;

      const next = {
        handle: jest.fn().mockReturnValueOnce(throwError(() => axiosError)),
      };

      interceptor.intercept(context, next).subscribe({
        next: () => {
          fail('Should have thrown error');
        },
        error: (error) => {
          expect(error).toBeInstanceOf(ForbiddenException);
          done();
        },
      });
    });

    it('should normalize 409 Conflict error', (done) => {
      const axiosError = {
        response: {
          status: 409,
          data: { error: 'INVALID_STATUS_TRANSITION', message: 'Status conflict' },
        },
      } as any;

      const next = {
        handle: jest.fn().mockReturnValueOnce(throwError(() => axiosError)),
      };

      interceptor.intercept(context, next).subscribe({
        next: () => {
          fail('Should have thrown error');
        },
        error: (error) => {
          expect(error).toBeInstanceOf(ConflictException);
          done();
        },
      });
    });

    it('should pass through non-Axios errors', (done) => {
      const regularError = new Error('Some error');

      const next = {
        handle: jest.fn().mockReturnValueOnce(throwError(() => regularError)),
      };

      interceptor.intercept(context, next).subscribe({
        next: () => {
          fail('Should have thrown error');
        },
        error: (error) => {
          expect(error).toBe(regularError);
          done();
        },
      });
    });

    it('should return successful response as-is', (done) => {
      const response = { status: 200, data: { id: 1, name: 'Test' } };

      const next = {
        handle: jest.fn().mockReturnValueOnce(of(response)),
      };

      interceptor.intercept(context, next).subscribe((result) => {
        expect(result).toEqual(response);
        done();
      });
    });
  });
});
