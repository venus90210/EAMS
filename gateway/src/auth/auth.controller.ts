import { Controller, Post, Body, Req, Res } from '@nestjs/common';
import { Request, Response } from 'express';
import { Public } from './public.decorator';
import { ProxyService } from '../proxy/proxy.service';
import { ValidatedUser } from './jwt.strategy';

@Controller('auth')
export class AuthController {
  constructor(private proxyService: ProxyService) {}

  @Public()
  @Post('login')
  async login(@Req() req: Request, @Res() res: Response) {
    // Create a pseudo-user object for the proxy (no auth needed for login)
    const pseudoUser: ValidatedUser = {
      userId: 'anonymous',
      role: 'GUEST',
    };

    try {
      const response = await this.proxyService.forward(req, pseudoUser).toPromise();
      res.status(response.status).json(response.data);
    } catch (error: any) {
      const status = error.response?.status || 500;
      const data = error.response?.data || { error: 'AUTH_ERROR', message: error.message };
      res.status(status).json(data);
    }
  }

  @Public()
  @Post('mfa/verify')
  async verifyMfa(@Req() req: Request, @Res() res: Response) {
    const pseudoUser: ValidatedUser = {
      userId: 'anonymous',
      role: 'GUEST',
    };

    try {
      const response = await this.proxyService.forward(req, pseudoUser).toPromise();
      res.status(response.status).json(response.data);
    } catch (error: any) {
      const status = error.response?.status || 500;
      const data = error.response?.data || { error: 'AUTH_ERROR', message: error.message };
      res.status(status).json(data);
    }
  }

  @Public()
  @Post('refresh')
  async refresh(@Req() req: Request, @Res() res: Response) {
    const pseudoUser: ValidatedUser = {
      userId: 'anonymous',
      role: 'GUEST',
    };

    try {
      const response = await this.proxyService.forward(req, pseudoUser).toPromise();
      res.status(response.status).json(response.data);
    } catch (error: any) {
      const status = error.response?.status || 500;
      const data = error.response?.data || { error: 'AUTH_ERROR', message: error.message };
      res.status(status).json(data);
    }
  }

  @Public()
  @Post('logout')
  async logout(@Req() req: Request, @Res() res: Response) {
    const pseudoUser: ValidatedUser = {
      userId: 'anonymous',
      role: 'GUEST',
    };

    try {
      const response = await this.proxyService.forward(req, pseudoUser).toPromise();
      res.status(response.status).json(response.data);
    } catch (error: any) {
      const status = error.response?.status || 500;
      const data = error.response?.data || { error: 'AUTH_ERROR', message: error.message };
      res.status(status).json(data);
    }
  }
}
